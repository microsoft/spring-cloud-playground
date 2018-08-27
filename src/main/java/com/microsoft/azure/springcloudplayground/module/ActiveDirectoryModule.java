package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.dependency.DependencyNames;
import com.microsoft.azure.springcloudplayground.service.Annotation;

import java.util.Arrays;

public class ActiveDirectoryModule extends Module{
    public ActiveDirectoryModule() {
        super(ModuleNames.AZURE_ACTIVE_DIRECTORY);
        this.getDependencies().addAll(Arrays.asList(
                DependencyNames.CLOUD_EUREKA_CLIENT,
                DependencyNames.CLOUD_CONFIG_CLIENT,
                DependencyNames.WEB,
                DependencyNames.AZURE_ACTIVE_DIRECTORY,
                DependencyNames.SPRING_THYMELEAF,
                DependencyNames.SPRING_SECURITY,
                DependencyNames.SPRING_SECURITY_OAUTH2_CLIENT,
                DependencyNames.SPRING_SECURITY_OAUTH2_JOSE,
                DependencyNames.THYMELEAF_EXTRAS
        ));

        this.annotations.add(Annotation.ENABLE_DISCOVERY_CLIENT);
    }
}
