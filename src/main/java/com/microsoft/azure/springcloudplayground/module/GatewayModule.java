package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import com.microsoft.azure.springcloudplayground.service.Dependencies;

import java.util.Arrays;

public class GatewayModule extends Module {

    public GatewayModule() {
        super(ModuleNames.CLOUD_GATEWAY);

        this.getDependencies().addAll(Arrays.asList(
                Dependencies.CLOUD_EUREKA_CLIENT,
                Dependencies.CLOUD_CONFIG_CLIENT,
                Dependencies.CLOUD_GATEWAY
        ));

        this.annotations.add(Annotation.ENABLE_DISCOVERY_CLIENT);
    }
}
