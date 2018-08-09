package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Module {
    private static final String BOOTSTAP_PROPERTIES_TEMPLATE = "bootstrap.properties";
    private static final String APPLICATION_PROPERTIES_TEMPLATE = "application.properties";
    private static final String TEMPLATE_DIR = "classpath:/templates/";

    @Getter
    private final List<String> dependencies = new ArrayList<>();
    protected final Set<Annotation> annotations = new HashSet<>();

    @Getter
    private final String name;

    public Module(String name) {
        this.name = name;
        this.annotations.add(Annotation.SPRING_BOOT_APPLICATION);
    }

    public Set<String> getAnnotations() {
        return this.annotations.stream().map(Annotation::getAnnotation).collect(Collectors.toSet());
    }

    public Set<String> getImports() {
        return this.annotations.stream().map(Annotation::getImports).collect(Collectors.toSet());
    }

    public String getBootstapPropsTemplate() {
        return TEMPLATE_DIR + name + File.separator + BOOTSTAP_PROPERTIES_TEMPLATE;
    }

    public String getApplicationPropsTemplate() {
        return TEMPLATE_DIR + name + File.separator + APPLICATION_PROPERTIES_TEMPLATE;
    }
}
