package com.microsoft.azure.springcloudplayground.service;

import com.microsoft.azure.springcloudplayground.generator.ProjectRequest;
import com.microsoft.azure.springcloudplayground.generator.ServiceLink;

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

    public static Service getService(String serviceName){
        if(!servicesByName.containsKey(serviceName)){
            throw new IllegalArgumentException(String.format("Service '%s' not existed.", serviceName));
        }
        return servicesByName.get(serviceName);
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

