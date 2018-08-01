package com.microsoft.azure.springcloudplayground.service;

import lombok.Getter;
import lombok.Setter;

public class ConfigurableService extends ServiceModule {

    @Getter
    @Setter
    private String applicationName;

    @Getter
    @Setter
    private String port;

    public ConfigurableService(String applicationName, String port) {
        super(applicationName, applicationName, "");
        this.applicationName = applicationName;
        this.port = port;
    }

    @Override
    public String getDescription() {
        return this.toString();
    }

    @Override
    public String toString() {
        return "ConfigurableService {" + "id='" + getId() + '\'' + ", name='" + getName()
                + '\'' + ", port='" + getPort() + '}';
    }
}
