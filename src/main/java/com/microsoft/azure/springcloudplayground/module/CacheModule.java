package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import com.microsoft.azure.springcloudplayground.dependency.DependencyNames;

import java.util.Arrays;

public class CacheModule extends Module {

    public CacheModule() {
        super(ModuleNames.AZURE_CACHE);
        this.getDependencies().addAll(Arrays.asList(
                DependencyNames.CLOUD_EUREKA_CLIENT,
                DependencyNames.CLOUD_CONFIG_CLIENT,
                DependencyNames.WEB,
                DependencyNames.AZURE_CACHE
        ));

        this.annotations.addAll(Arrays.asList(Annotation.ENABLE_CACHE, Annotation.ENABLE_DISCOVERY_CLIENT));
    }
}
