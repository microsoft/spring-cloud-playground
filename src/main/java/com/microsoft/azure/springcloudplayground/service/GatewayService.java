package com.microsoft.azure.springcloudplayground.service;

import java.util.Arrays;

class GatewayService extends Service {

    GatewayService(int port) {
        super(Modules.CLOUD_GATEWAY, port, "/");

        this.getDependencies().addAll(Arrays.asList(Dependencies.CLOUD_EUREKA_CLIENT, Dependencies
                .CLOUD_CONFIG_CLIENT, Dependencies.CLOUD_GATEWAY));
        this.annotations.add(Annotation.ENABLE_DISCOVERY_CLIENT);
    }

    GatewayService() {
        this(9999);
    }
}
