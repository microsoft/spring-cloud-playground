package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import com.microsoft.azure.springcloudplayground.dependency.DependencyNames;
import com.microsoft.azure.springcloudplayground.service.ServiceNames;

import java.util.Arrays;

public class EurekaModule extends Module {

    public EurekaModule() {
        super(ServiceNames.CLOUD_EUREKA_SERVER);

        this.getDependencies().addAll(Arrays.asList(
                DependencyNames.CLOUD_EUREKA_SERVER,
                DependencyNames.CLOUD_CONFIG_CLIENT));
        this.annotations.add(Annotation.ENABLE_EUREKA_SERVER);
    }
}
