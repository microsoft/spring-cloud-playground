package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.dependency.DependencyNames;

import java.util.Arrays;

public class CosmosdbModule extends Module {

    public CosmosdbModule() {
        super(ModuleNames.AZURE_COSMOSDB);
        this.getDependencies().addAll(Arrays.asList(
                DependencyNames.CLOUD_EUREKA_CLIENT,
                DependencyNames.CLOUD_CONFIG_SERVER,
                DependencyNames.AZURE_COSMOSDB,
                DependencyNames.SPRING_DATA_COSMOSDB,
                DependencyNames.WEB
        ));
    }
}
