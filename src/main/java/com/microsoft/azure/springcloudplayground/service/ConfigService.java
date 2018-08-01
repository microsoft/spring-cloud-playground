package com.microsoft.azure.springcloudplayground.service;

class ConfigService extends Service {

    ConfigService(){
        super(Modules.CLOUD_CONFIG_SERVER, 8888, "/gateway-server.yml");
        this.getDependencies().add(Dependencies.CLOUD_CONFIG_SERVER);
        this.annotations.add(Annotation.ENABLE_CONFIG_SERVER);
    }
}
