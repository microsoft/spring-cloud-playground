package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Module {

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
}
