package com.microsoft.azure.springcloudplayground.service;

import com.microsoft.azure.springcloudplayground.generator.ServiceLink;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class Service {
    private static final String DEFAULT_HEALTH_CHECK_PATH = "/%s/actuator/health";
    private static final String DEFAULT_DOCKER_IMAGE = "/demo/%s";
    private static final String DEFAULT_URL = "http://localhost:%s/";

    private final String name;
    private final String healthCheckPath;
    private ServiceLink serviceLink;

    private final List<String> dependencies = new ArrayList<>();
    protected final Set<Annotation> annotations = new HashSet<>();

    private int port;

    public String getDockerImage(){
        return String.format(DEFAULT_DOCKER_IMAGE, this.name);
    }

    Service(String name, int port){
        this(name, port, String.format(DEFAULT_HEALTH_CHECK_PATH, name));
    }

    Service(String name, int port, String healthCheckPath){
        this.name = name;
        this.port = port;
        this.healthCheckPath = healthCheckPath;
        this.annotations.add(Annotation.SPRING_BOOT_APPLICATION);
        this.serviceLink = new ServiceLink(name, String.format(DEFAULT_URL, port));
    }

    public Set<String> getAnnotations(){
        return this.annotations.stream().map(Annotation::getAnnotation).collect(Collectors.toSet());
    }

    public Set<String> getImports(){
        return this.annotations.stream().map(Annotation::getImports).collect(Collectors.toSet());
    }

}
