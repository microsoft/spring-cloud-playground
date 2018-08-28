package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.dependency.DependencyNames;
import com.microsoft.azure.springcloudplayground.service.Annotation;

import java.util.Arrays;

public class CosmosdbModule extends Module {

    public CosmosdbModule() {
        super(ModuleNames.AZURE_COSMOSDB);
        this.getDependencies().addAll(Arrays.asList(
                DependencyNames.CLOUD_EUREKA_CLIENT,
                DependencyNames.CLOUD_CONFIG_CLIENT,
                DependencyNames.AZURE_COSMOSDB,
                DependencyNames.SPRING_DATA_COSMOSDB,
                DependencyNames.WEB
        ));

        this.annotations.add(Annotation.ENABLE_DISCOVERY_CLIENT);
    }
}
