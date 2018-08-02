package com.microsoft.azure.springcloudplayground.service;

class ConfigService extends Service {

    ConfigService(int port) {
        super(ServiceNames.CLOUD_CONFIG_SERVER, port, "/cloud-gateway.yml");
        this.getDependencies().add(Dependencies.CLOUD_CONFIG_SERVER);
        this.annotations.add(Annotation.ENABLE_CONFIG_SERVER);
    }

    ConfigService() {
        this(8888);
    }
}
