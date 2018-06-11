package com.microsoft.azure.springcloudplayground.generator;

import com.microsoft.azure.springcloudplayground.util.VersionProperty;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

public class BuildProperties {

    /**
     * Maven-specific build properties, added to the regular {@code properties} element.
     */
    private final TreeMap<String, Supplier<String>> maven = new TreeMap<>();

    /**
     * Gradle-specific build properties, added to the {@code buildscript} section of the
     * gradle build.
     */
    private final TreeMap<String, Supplier<String>> gradle = new TreeMap<>();

    /**
     * Version properties. Shared between the two build systems.
     */
    private final TreeMap<VersionProperty, Supplier<String>> versions = new TreeMap<>();

    public Map<String, Supplier<String>> getMaven() {
        return this.maven;
    }

    public Map<String, Supplier<String>> getGradle() {
        return this.gradle;
    }

    public Map<VersionProperty, Supplier<String>> getVersions() {
        return this.versions;
    }

}
