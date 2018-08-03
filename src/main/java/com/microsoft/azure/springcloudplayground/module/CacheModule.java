package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import com.microsoft.azure.springcloudplayground.service.Dependencies;

import java.util.Arrays;

public class CacheModule extends Module {

    public CacheModule() {
        super(ModuleNames.AZURE_CACHE);
        this.getDependencies().addAll(Arrays.asList(
                Dependencies.CLOUD_EUREKA_CLIENT,
                Dependencies.CLOUD_CONFIG_CLIENT,
                Dependencies.WEB,
                Dependencies.AZURE_CACHE
        ));

        this.annotations.addAll(Arrays.asList(Annotation.ENABLE_CACHE, Annotation.ENABLE_DISCOVERY_CLIENT));
    }
}
