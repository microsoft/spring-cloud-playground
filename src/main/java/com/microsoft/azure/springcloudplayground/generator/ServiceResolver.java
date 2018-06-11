package com.microsoft.azure.springcloudplayground.generator;

import com.microsoft.azure.springcloudplayground.service.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ServiceResolver {

    public static List<Service> resolve(Iterable<String> serviceNames){
        return StreamSupport.stream(serviceNames.spliterator(), false).map(Service::toService).collect(Collectors.toList());
    }
}
