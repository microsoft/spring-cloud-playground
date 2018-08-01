package com.microsoft.azure.springcloudplayground.service;

import java.util.Arrays;

class CacheService extends Service {

    CacheService(){
        super(Modules.AZURE_CACHE, 8081);
        this.getDependencies().addAll(Arrays.asList(Dependencies.CLOUD_EUREKA_CLIENT, Dependencies
                .CLOUD_CONFIG_CLIENT, Dependencies.WEB, Dependencies.AZURE_CACHE));

        this.annotations.addAll(Arrays.asList(Annotation.ENABLE_CACHE, Annotation
                .ENABLE_DISCOVERY_CLIENT));
    }
}
