package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.service.Annotation;
import com.microsoft.azure.springcloudplayground.dependency.DependencyNames;

import java.util.Arrays;

public class EventHubModule extends Module {

    public EventHubModule(){
        super(ModuleNames.AZURE_EVNET_HUB_BINDER);
        this.getDependencies().addAll(Arrays.asList(
                DependencyNames.CLOUD_EUREKA_CLIENT,
                DependencyNames.CLOUD_CONFIG_CLIENT,
                DependencyNames.AZURE_EVNET_HUB_BINDER,
                DependencyNames.AZURE_EVNET_HUB_STARTER,
                DependencyNames.WEB
        ));

        this.annotations.addAll(Arrays.asList(Annotation.ENABLE_CACHE, Annotation.ENABLE_DISCOVERY_CLIENT));
    }
}
