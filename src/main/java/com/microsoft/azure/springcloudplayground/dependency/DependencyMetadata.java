package com.microsoft.azure.springcloudplayground.dependency;

import com.microsoft.azure.springcloudplayground.metadata.BillOfMaterials;
import com.microsoft.azure.springcloudplayground.metadata.Repository;
import com.microsoft.azure.springcloudplayground.util.Version;
import lombok.Getter;

import java.util.Map;

public class DependencyMetadata {

    @Getter
    final Version bootVersion;

    @Getter
    final Map<String, Dependency> dependencies;

    @Getter
    final Map<String, Repository> repositories;

    @Getter
    final Map<String, BillOfMaterials> boms;

    public DependencyMetadata() {
        this(null, null, null, null);
    }

    public DependencyMetadata(Version bootVersion, Map<String, Dependency> dependencies,
                              Map<String, Repository> repositories, Map<String, BillOfMaterials> boms) {
        this.bootVersion = bootVersion;
        this.dependencies = dependencies;
        this.repositories = repositories;
        this.boms = boms;
    }
}
