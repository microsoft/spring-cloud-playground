package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import com.microsoft.azure.springcloudplayground.dependency.DependencyNames;

import java.util.Arrays;

public class HystrixDashboardModule extends Module {

    public HystrixDashboardModule() {
        super(ModuleNames.CLOUD_HYSTRIX_DASHBOARD);

        this.getDependencies().addAll(Arrays.asList(
                DependencyNames.CLOUD_HYSTRIX_DASHBOARD,
                DependencyNames.CLOUD_CONFIG_CLIENT,
                DependencyNames.CLOUD_EUREKA_CLIENT,
                DependencyNames.WEB
        ));
        this.annotations.addAll(Arrays.asList(Annotation.ENABLE_DISCOVERY_CLIENT, Annotation.ENABLE_HYSTRIX_DASHBOARD));
    }
}
