package com.microsoft.azure.springcloudplayground.service;

import java.util.Arrays;

class EurekaService extends Service {

    EurekaService(int port) {
        super(ServiceNames.CLOUD_EUREKA_SERVER, port, "/");

        this.getDependencies().addAll(Arrays.asList(Dependencies.CLOUD_EUREKA_SERVER,
                Dependencies.CLOUD_CONFIG_CLIENT));
        this.annotations.add(Annotation.ENABLE_EUREKA_SERVER);
    }

    EurekaService() {
        this(8761);
    }
}
