package com.microsoft.azure.springcloudplayground.service;

import java.util.Arrays;

class EurekaService extends Service {

    EurekaService(){
        super(Modules.CLOUD_EUREKA_SERVER, 8761, "/");
        this.getDependencies().addAll(Arrays.asList(Dependencies.CLOUD_EUREKA_SERVER, Dependencies
                .CLOUD_CONFIG_CLIENT));

        this.annotations.add(Annotation.ENABLE_EUREKA_SERVER);
    }
}
