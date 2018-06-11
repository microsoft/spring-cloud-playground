package com.microsoft.azure.springcloudplayground.dependency;

import com.microsoft.azure.springcloudplayground.metadata.BillOfMaterials;
import com.microsoft.azure.springcloudplayground.metadata.Repository;
import com.microsoft.azure.springcloudplayground.util.Version;

import java.util.Map;

public class DependencyMetadata {

    final Version bootVersion;

    final Map<String, Dependency> dependencies;

    final Map<String, Repository> repositories;

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

    public Version getBootVersion() {
        return this.bootVersion;
    }

    public Map<String, Dependency> getDependencies() {
        return this.dependencies;
    }

    public Map<String, Repository> getRepositories() {
        return this.repositories;
    }

    public Map<String, BillOfMaterials> getBoms() {
        return this.boms;
    }

}
