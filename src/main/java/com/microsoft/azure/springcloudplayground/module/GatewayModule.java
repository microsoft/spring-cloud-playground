package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import com.microsoft.azure.springcloudplayground.dependency.DependencyNames;

import java.util.Arrays;

public class GatewayModule extends Module {

    public GatewayModule() {
        super(ModuleNames.CLOUD_GATEWAY);

        this.getDependencies().addAll(Arrays.asList(
                DependencyNames.CLOUD_EUREKA_CLIENT,
                DependencyNames.CLOUD_CONFIG_CLIENT,
                DependencyNames.CLOUD_GATEWAY
        ));

        this.annotations.add(Annotation.ENABLE_DISCOVERY_CLIENT);
    }
}
