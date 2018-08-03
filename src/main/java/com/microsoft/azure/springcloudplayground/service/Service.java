package com.microsoft.azure.springcloudplayground.service;

import com.microsoft.azure.springcloudplayground.module.Module;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Builder(builderMethodName = "hiddenBuilder")
public class Service {

    @Getter
    private String name;

    private int port;

    private boolean isAzure;

    private String k8sHealthCheck;

    private String dockerComposeHealthCheck;

    private String homePage;

    private String k8sDockerImage;

    private String dockerComposeImage;

    private List<String> links;

    private List<String> dependsOn;

    private List<Module> modules;

    public static ServiceBuilder builder(@NonNull String name, int port) {
        return hiddenBuilder()
                .name(name)
                .port(port)
                .k8sHealthCheck(String.format("/%s/actuator/health", name))
                .k8sDockerImage(String.format("/demo/%s", name))
                .dockerComposeHealthCheck(String.format("http://%s:%d/actuator/health", name, port))
                .dockerComposeImage(String.format("demo/%s", name));
    }

    public Set<String> getAnnotations() {
        return modules.stream().map(Module::getAnnotations).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public Set<String> getImports() {
        return modules.stream().map(Module::getImports).flatMap(Collection::stream).collect(Collectors.toSet());
    }
}
