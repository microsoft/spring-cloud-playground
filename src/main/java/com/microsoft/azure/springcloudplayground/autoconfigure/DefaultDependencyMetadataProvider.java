package com.microsoft.azure.springcloudplayground.autoconfigure;

import com.microsoft.azure.springcloudplayground.dependency.Dependency;
import com.microsoft.azure.springcloudplayground.dependency.DependencyMetadata;
import com.microsoft.azure.springcloudplayground.dependency.DependencyMetadataProvider;
import com.microsoft.azure.springcloudplayground.metadata.*;
import com.microsoft.azure.springcloudplayground.util.Version;
import org.springframework.cache.annotation.Cacheable;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultDependencyMetadataProvider implements DependencyMetadataProvider {

    @Override
    @Cacheable(cacheNames = "playground.dependency-metadata", key = "#p1")
    public DependencyMetadata get(GeneratorMetadata metadata, Version bootVersion) {
        Map<String, Dependency> dependencies = new LinkedHashMap<>();
        for (Dependency d : metadata.getDependencies().getAll()) {
            if (d.match(bootVersion)) {
                dependencies.put(d.getId(), d.resolve(bootVersion));
            }
        }

        Map<String, Repository> repositories = new LinkedHashMap<>();
        for (Dependency d : dependencies.values()) {
            if (d.getRepository() != null) {
                repositories.put(d.getRepository(), metadata.getConfiguration().getEnv()
                        .getRepositories().get(d.getRepository()));
            }
        }

        Map<String, BillOfMaterials> boms = new LinkedHashMap<>();
        for (Dependency d : dependencies.values()) {
            if (d.getBom() != null) {
                boms.put(d.getBom(), metadata.getConfiguration().getEnv().getBoms()
                        .get(d.getBom()).resolve(bootVersion));
            }
        }
        // Each resolved bom may require additional repositories
        for (BillOfMaterials b : boms.values()) {
            for (String id : b.getRepositories()) {
                repositories.put(id,
                        metadata.getConfiguration().getEnv().getRepositories().get(id));
            }
        }

        return new DependencyMetadata(bootVersion, dependencies, repositories, boms);
    }

}
