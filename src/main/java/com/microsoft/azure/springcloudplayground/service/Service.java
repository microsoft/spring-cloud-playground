package com.microsoft.azure.springcloudplayground.service;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Service {
    CONFIG("cloud-config-server", 8888, "/gateway-server.yml", "demo/cloud-config-server"),
    EUREKA("cloud-eureka-server", 8761, "/", "demo/cloud-eureka-server"),
    HYSTRIX_DASHBOARD("cloud-hystrix-dashboard", 7979, "/hystrix", "demo/cloud-hystrix-dashboard"),
    GATEWAY("cloud-gateway", 9999, "/actuator/health", "demo/cloud-gateway"),
    AZURE_SERVICE_BUS("azure-service-bus", 8081, "/azure-service-bus/actuator/health", "demo/azure-service-bus");

    private final String name;
    private final int port;
    private final String healthCheckPath;
    private final String image;

    private static Map<String, Service> nameToServices = Arrays.stream(Service.values()).collect(Collectors.toMap(Service::getName, Function.identity()));

    Service(String name, int port, String healthCheckPath, String image) {
        this.name = name;
        this.port = port;
        this.healthCheckPath = healthCheckPath;
        this.image = image;
    }

    public static Service toService(String name){
        if(!nameToServices.containsKey(name)){
            throw new IllegalArgumentException(String.format("Invalid Service with name '%s'", name));
        }

        return nameToServices.get(name);
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public String getHealthCheckPath() {
        return healthCheckPath;
    }

    public String getImage() {
        return image;
    }
}
