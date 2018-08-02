package com.microsoft.azure.springcloudplayground.service;

import com.microsoft.azure.springcloudplayground.generator.MicroService;
import com.microsoft.azure.springcloudplayground.module.ModuleNames;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceNames {

    public static final String CLOUD_GATEWAY = "cloud-gateway";

    public static final String CLOUD_CONFIG_SERVER = "cloud-config-server";

    public static final String CLOUD_EUREKA_SERVER = "cloud-eureka-server";

    public static final String CLOUD_HYSTRIX_DASHBOARD = "cloud-hystrix-dashboard";

    public static Service toService(@NonNull MicroService microService) {
        String name = microService.getName();
        int port = microService.getPort();
        List<String> modules = microService.getModules();

        switch (microService.getName()) {
            case ServiceNames.CLOUD_CONFIG_SERVER:
                return Service.builder(name, port)
                        .modules(modules.stream().map(ModuleNames::toModule).collect(Collectors.toList()))
                        .build();
            case ServiceNames.CLOUD_EUREKA_SERVER:
                return Service.builder(name, port)
                        .modules(modules.stream().map(ModuleNames::toModule).collect(Collectors.toList()))
                        .links(Arrays.asList(CLOUD_CONFIG_SERVER))
                        .dependsOn(Arrays.asList(CLOUD_CONFIG_SERVER))
                        .build();
            case ServiceNames.CLOUD_GATEWAY:
                return Service.builder(name, port)
                        .modules(modules.stream().map(ModuleNames::toModule).collect(Collectors.toList()))
                        .links(Arrays.asList(CLOUD_EUREKA_SERVER, CLOUD_CONFIG_SERVER))
                        .dependsOn(Arrays.asList(CLOUD_EUREKA_SERVER))
                        .build();
            case ServiceNames.CLOUD_HYSTRIX_DASHBOARD:
                return Service.builder(name, port)
                        .modules(modules.stream().map(ModuleNames::toModule).collect(Collectors.toList()))
                        .links(Arrays.asList(CLOUD_EUREKA_SERVER, CLOUD_CONFIG_SERVER))
                        .dependsOn(Arrays.asList(CLOUD_EUREKA_SERVER))
                        .build();
            default:
                return Service.builder(name, port)
                        .modules(modules.stream().map(ModuleNames::toModule).collect(Collectors.toList()))
                        .links(Arrays.asList(CLOUD_EUREKA_SERVER, CLOUD_CONFIG_SERVER))
                        .dependsOn(Arrays.asList(CLOUD_EUREKA_SERVER))
                        .build();
        }
    }
}
