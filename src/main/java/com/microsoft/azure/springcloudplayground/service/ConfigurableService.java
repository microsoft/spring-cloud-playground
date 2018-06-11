package com.microsoft.azure.springcloudplayground.service;

public class ConfigurableService extends ServiceModule {
    private String applicationName;
    private String port;

    public ConfigurableService(String applicationName, String port) {
        super("", applicationName, "");
        this.applicationName = applicationName;
        this.port = port;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
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
