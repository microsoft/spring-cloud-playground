package com.microsoft.azure.springcloudplayground.service;

import org.springframework.lang.NonNull;

import java.util.*;

public class ServiceMetadata {
    private final static List<ServiceLink> serviceLinks = new ArrayList<>();
    private final static Map<String, Service> servicesByName = new HashMap<>();

    static {
        Set<Service> services = new HashSet<>(Arrays.asList(new ConfigService(), new EurekaService(), new
                GatewayService(), new HystrixDashboardService(), new CacheService(), new StorageService(), new
                SqlServerService(), new EventHubService()));
        services.forEach(i -> servicesByName.put(i.getName(), i));
        services.forEach(i -> serviceLinks.add(i.getServiceLink()));
    }

    public static Service getService(String serviceName) {
        if (!servicesByName.containsKey(serviceName)) {
            throw new IllegalArgumentException(String.format("Service '%s' not existed.", serviceName));
        }
        return servicesByName.get(serviceName);
    }

    public static Map<String, List<ServiceLink>> getLinksMap(@NonNull List<String> services) {
        Map<String, List<ServiceLink>> linksMap = new HashMap<>();
        List<ServiceLink> filteredLinks = new ArrayList<>();

        for (String service : services) {
            Optional<ServiceLink> linkOp = serviceLinks.stream().filter(link -> service.equals(link.getServiceName())).findFirst();
            if (!linkOp.isPresent()) {
                throw new IllegalStateException("Failed to find ServiceLink for module " + service);
            }

            filteredLinks.add(linkOp.get());
        }

        linksMap.put("services", filteredLinks);
        return linksMap;
    }
}

