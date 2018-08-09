package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import com.microsoft.azure.springcloudplayground.dependency.DependencyNames;

import java.util.Arrays;

public class StorageModule extends Module {

    public StorageModule() {
        super(ModuleNames.AZURE_STORAGE);
        this.getDependencies().addAll(Arrays.asList(
                DependencyNames.CLOUD_EUREKA_CLIENT,
                DependencyNames.CLOUD_CONFIG_CLIENT,
                DependencyNames.AZURE_STORAGE,
                DependencyNames.WEB
        ));

        this.annotations.add(Annotation.ENABLE_DISCOVERY_CLIENT);
    }
}
