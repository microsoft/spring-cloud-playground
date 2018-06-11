package com.microsoft.azure.springcloudplayground.generator;

import java.util.*;

public class ServiceMetadata {
    public final static Map<String, List<String>> annotationMap = new HashMap<>();

    public final static Map<String, List<String>> importMap = new HashMap<>();

    public final static Map<String, Integer> portMap = new HashMap<>();

    private final static List<ServiceLink> serviceLinks = new ArrayList<>();

    static {
        final String azureServiceBus = "azure-service-bus";
        final String cloudConfigServer = "cloud-config-server";
        final String cloudEurekaServer = "cloud-eureka-server";
        final String cloudGateway = "cloud-gateway";
        final String cloudHystrixDashboard = "cloud-hystrix-dashboard";

        // Azure Service Bus
        annotationMap.put(azureServiceBus, Arrays.asList("@SpringBootApplication", "@EnableDiscoveryClient"));
        importMap.put(azureServiceBus, Arrays.asList(
                "org.springframework.boot.autoconfigure.SpringBootApplication",
                "org.springframework.cloud.client.discovery.EnableDiscoveryClient"));
        portMap.put(azureServiceBus, 8081);
        // Use relative path for services routed by gateway
        serviceLinks.add(new ServiceLink(azureServiceBus, "/" + azureServiceBus + "/", false));

        // Cloud Config Server
        annotationMap.put(cloudConfigServer, Arrays.asList("@SpringBootApplication", "@EnableConfigServer"));
        importMap.put(cloudConfigServer, Arrays.asList(
                "org.springframework.boot.autoconfigure.SpringBootApplication",
                "org.springframework.cloud.config.server.EnableConfigServer"));
        portMap.put(cloudConfigServer, 8888);
        serviceLinks.add(new ServiceLink(cloudConfigServer, "http://localhost:8888/application-native.yml", true));

        // Cloud Eureka Server
        annotationMap.put(cloudEurekaServer, Arrays.asList("@SpringBootApplication", "@EnableEurekaServer"));
        importMap.put(cloudEurekaServer, Arrays.asList(
                "org.springframework.boot.autoconfigure.SpringBootApplication",
                "org.springframework.cloud.netflix.eureka.server.EnableEurekaServer"));
        portMap.put(cloudEurekaServer, 8761);
        serviceLinks.add(new ServiceLink(cloudEurekaServer, "http://localhost:8761/", true));

        // Cloud Gateway
        annotationMap.put(cloudGateway, Arrays.asList("@SpringBootApplication", "@EnableDiscoveryClient"));
        importMap.put(cloudGateway, Arrays.asList(
                "org.springframework.boot.autoconfigure.SpringBootApplication",
                "org.springframework.cloud.client.discovery.EnableDiscoveryClient"));
        portMap.put(cloudGateway, 9999);
        serviceLinks.add(new ServiceLink(cloudGateway, "http://localhost:9999/", true));

        // Cloud Hystrix Dashboard
        annotationMap.put(cloudHystrixDashboard, Arrays.asList("@SpringBootApplication", "@EnableDiscoveryClient", "@EnableHystrixDashboard"));
        importMap.put(cloudHystrixDashboard, Arrays.asList(
                "org.springframework.boot.autoconfigure.SpringBootApplication",
                "org.springframework.cloud.client.discovery.EnableDiscoveryClient",
                "org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard"));
        portMap.put(cloudHystrixDashboard, 7979);
        serviceLinks.add(new ServiceLink(cloudHystrixDashboard, "http://localhost:7979/hystrix", true));
    }

    public static Map<String, List<ServiceLink>> getLinksMap(ProjectRequest rootRequest) {
        Map<String, List<ServiceLink>> linksMap = new HashMap<>();
        List<ServiceLink> filteredLinks = new ArrayList<>();

        for(ProjectRequest subModule: rootRequest.getModules()){
            String moduleName = subModule.getName();
            Optional<ServiceLink> linkOp = serviceLinks.stream().filter(link -> moduleName.equals(link.getServiceName())).findFirst();
            if(!linkOp.isPresent()) {
                throw new IllegalStateException("Failed to find ServiceLink for module " + moduleName);
            }

            filteredLinks.add(linkOp.get());
        }

        linksMap.put("services", filteredLinks);
        return linksMap;
    }
}

