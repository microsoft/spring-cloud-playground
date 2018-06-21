/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.springcloudplayground.util;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

public class TelemetryProxy {

    private static final String PROJECT_PROPERTY_FILE = "/META-INF/project.properties";

    private final TelemetryClient client;
    private final PropertyLoader loader;

    public TelemetryProxy() {
        this.client = new TelemetryClient();
        this.loader = new PropertyLoader(PROJECT_PROPERTY_FILE);
    }

    public void trackEvent(@NonNull String name, @Nullable Map<String, String> customProperties) {
        this.trackEvent(name, customProperties, false);
    }

    public void trackEvent(@NonNull String name, @Nullable Map<String, String> customProperties, boolean isOverride) {
        Map<String, String> properties = this.getDefaultProperties();
        properties = this.mergeProperties(properties, customProperties, isOverride);
        client.trackEvent(name, properties, null);
        client.flush();
    }

    private Map<String, String> mergeProperties(@NonNull Map<String, String> defaultProperties,
                                                @Nullable Map<String, String> customProperties, boolean isOverride) {
        if (customProperties == null || customProperties.isEmpty()) {
            return defaultProperties;
        }

        final Map<String, String> merged = new HashMap<>();

        if (isOverride) {
            merged.putAll(defaultProperties);
            merged.putAll(customProperties);
        }
        else {
            merged.putAll(customProperties);
            merged.putAll(defaultProperties);
        }

        merged.entrySet().stream().filter(e -> e.getValue().isEmpty()).forEach(p -> merged.remove(p.getKey()));

        return merged;
    }

    private Map<String, String> getDefaultProperties() {
        final Map<String, String> properties = new HashMap<>();
        final String version = "spring-cloud-playground/" + this.loader.getPropertyValue("project.version");

        properties.put("version", version);

        return properties;
    }
}

