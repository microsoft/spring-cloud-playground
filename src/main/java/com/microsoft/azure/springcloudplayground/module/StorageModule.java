package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import com.microsoft.azure.springcloudplayground.service.Dependencies;

import java.util.Arrays;

public class StorageModule extends Module {

    public StorageModule() {
        super(ModuleNames.AZURE_STORAGE);
        this.getDependencies().addAll(Arrays.asList(
                Dependencies.CLOUD_EUREKA_CLIENT,
                Dependencies.CLOUD_CONFIG_CLIENT,
                Dependencies.AZURE_STORAGE,
                Dependencies.WEB
        ));

        this.annotations.add(Annotation.ENABLE_DISCOVERY_CLIENT);
    }
}
