package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import com.microsoft.azure.springcloudplayground.service.Dependencies;
import com.microsoft.azure.springcloudplayground.service.ServiceNames;

import java.util.Arrays;

public class EurekaModule extends Module {

    public EurekaModule() {
        super(ServiceNames.CLOUD_EUREKA_SERVER);

        this.getDependencies().addAll(Arrays.asList(
                Dependencies.CLOUD_EUREKA_SERVER,
                Dependencies.CLOUD_CONFIG_CLIENT));
        this.annotations.add(Annotation.ENABLE_EUREKA_SERVER);
    }
}
