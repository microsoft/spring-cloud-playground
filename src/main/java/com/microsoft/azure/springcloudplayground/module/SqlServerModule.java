package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import com.microsoft.azure.springcloudplayground.service.Dependencies;

import java.util.Arrays;

public class SqlServerModule extends Module {

    public SqlServerModule() {
        super(ModuleNames.AZURE_SQL_SERVER);
        this.getDependencies().addAll(Arrays.asList(
                Dependencies.CLOUD_EUREKA_CLIENT,
                Dependencies.CLOUD_CONFIG_CLIENT,
                Dependencies.AZURE_SQL_SERVER,
                Dependencies.WEB
        ));

        this.annotations.add(Annotation.ENABLE_DISCOVERY_CLIENT);
    }
}
