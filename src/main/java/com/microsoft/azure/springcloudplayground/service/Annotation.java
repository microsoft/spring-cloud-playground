package com.microsoft.azure.springcloudplayground.service;

import lombok.Getter;

@Getter
public enum Annotation {
    SPRING_BOOT_APPLICATION("@SpringBootApplication", "org.springframework.boot.autoconfigure.SpringBootApplication"),
    ENABLE_CONFIG_SERVER("@EnableConfigServer", "org.springframework.cloud.config.server.EnableConfigServer"),
    ENABLE_EUREKA_SERVER("@EnableEurekaServer", "org.springframework.cloud.netflix.eureka.server.EnableEurekaServer"),
    ENABLE_HYSTRIX_DASHBOARD("@EnableHystrixDashboard",
            "org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard"),
    ENABLE_DISCOVERY_CLIENT("@EnableDiscoveryClient",
            "org.springframework.cloud.client.discovery.EnableDiscoveryClient"),
    ENABLE_CACHE("@EnableCaching", "org.springframework.cache.annotation.EnableCaching");

    private final String annotation;
    private final String imports;

    Annotation(String annotation, String imports) {
        this.annotation = annotation;
        this.imports = imports;
    }
}
