package com.microsoft.azure.springcloudplayground.generator;

import com.microsoft.azure.springcloudplayground.service.Service;
import com.microsoft.azure.springcloudplayground.service.ServiceMetadata;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ServiceResolver {

    public static List<Service> resolve(Iterable<String> serviceNames){
        return StreamSupport.stream(serviceNames.spliterator(), false).map(ServiceMetadata::getService).collect(Collectors
                .toList());
    }
}
