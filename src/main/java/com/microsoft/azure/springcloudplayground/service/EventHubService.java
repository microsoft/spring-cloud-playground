package com.microsoft.azure.springcloudplayground.service;

import java.util.Arrays;

class EventHubService extends Service {

    EventHubService(){
        super(Modules.AZURE_EVNET_HUB_BINDER, 8084);
        this.getDependencies().addAll(Arrays.asList(Dependencies.CLOUD_EUREKA_CLIENT, Dependencies
                .CLOUD_CONFIG_CLIENT, Dependencies.AZURE_EVNET_HUB_BINDER, Dependencies.AZURE_EVNET_HUB_STARTER,
                Dependencies.WEB));

        this.annotations.addAll(Arrays.asList(Annotation.ENABLE_CACHE, Annotation
                .ENABLE_DISCOVERY_CLIENT));
    }
}
