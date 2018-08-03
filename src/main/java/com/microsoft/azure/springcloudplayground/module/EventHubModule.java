package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import com.microsoft.azure.springcloudplayground.service.Dependencies;

import java.util.Arrays;

public class EventHubModule extends Module {

    public EventHubModule(){
        super(ModuleNames.AZURE_EVNET_HUB_BINDER);
        this.getDependencies().addAll(Arrays.asList(
                Dependencies.CLOUD_EUREKA_CLIENT,
                Dependencies.CLOUD_CONFIG_CLIENT,
                Dependencies.AZURE_EVNET_HUB_BINDER,
                Dependencies.AZURE_EVNET_HUB_STARTER,
                Dependencies.WEB
        ));

        this.annotations.addAll(Arrays.asList(Annotation.ENABLE_CACHE, Annotation.ENABLE_DISCOVERY_CLIENT));
    }
}
