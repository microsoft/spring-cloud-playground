package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import com.microsoft.azure.springcloudplayground.dependency.DependencyNames;

import java.util.Arrays;

public class SqlServerModule extends Module {

    public SqlServerModule() {
        super(ModuleNames.AZURE_SQL_SERVER);
        this.getDependencies().addAll(Arrays.asList(
                DependencyNames.CLOUD_EUREKA_CLIENT,
                DependencyNames.CLOUD_CONFIG_CLIENT,
                DependencyNames.AZURE_SQL_SERVER,
                DependencyNames.WEB,
                DependencyNames.JPA
        ));

        this.annotations.add(Annotation.ENABLE_DISCOVERY_CLIENT);
    }
}
