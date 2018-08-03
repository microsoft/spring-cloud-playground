package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import com.microsoft.azure.springcloudplayground.service.Dependencies;
import com.microsoft.azure.springcloudplayground.service.ServiceNames;

public class ConfigModule extends Module {

    public ConfigModule() {
        super(ServiceNames.CLOUD_CONFIG_SERVER);

        this.getDependencies().add(Dependencies.CLOUD_CONFIG_SERVER);
        this.annotations.add(Annotation.ENABLE_CONFIG_SERVER);
    }
}
