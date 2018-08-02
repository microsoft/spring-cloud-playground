package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import com.microsoft.azure.springcloudplayground.service.Dependencies;

import java.util.Arrays;

public class HystrixDashboardModule extends Module {

    public HystrixDashboardModule() {
        super(ModuleNames.CLOUD_HYSTRIX_DASHBOARD);

        this.getDependencies().addAll(Arrays.asList(
                Dependencies.CLOUD_HYSTRIX_DASHBOARD,
                Dependencies.CLOUD_CONFIG_CLIENT,
                Dependencies.CLOUD_EUREKA_CLIENT,
                Dependencies.WEB
        ));
        this.annotations.addAll(Arrays.asList(Annotation.ENABLE_DISCOVERY_CLIENT, Annotation.ENABLE_HYSTRIX_DASHBOARD));
    }
}
