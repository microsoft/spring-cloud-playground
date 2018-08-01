package com.microsoft.azure.springcloudplayground.service;

import java.util.Arrays;

class GatewayService extends Service {

    GatewayService(){
        super(Modules.CLOUD_GATEWAY, 9999, "/");
        this.getDependencies().addAll(Arrays.asList(Dependencies.CLOUD_EUREKA_CLIENT, Dependencies
                .CLOUD_CONFIG_CLIENT, Dependencies.CLOUD_GATEWAY));

        this.annotations.add(Annotation.ENABLE_DISCOVERY_CLIENT);
    }
}
