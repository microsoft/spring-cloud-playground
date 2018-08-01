package com.microsoft.azure.springcloudplayground.service;

import java.util.Arrays;

class StorageService extends Service {

    StorageService(){
        super(Modules.AZURE_STORAGE, 8082);
        this.getDependencies().addAll(Arrays.asList(Dependencies.CLOUD_EUREKA_CLIENT, Dependencies
                .CLOUD_CONFIG_CLIENT, Dependencies.AZURE_STORAGE, Dependencies.WEB));

        this.annotations.add(Annotation.ENABLE_DISCOVERY_CLIENT);
    }
}
