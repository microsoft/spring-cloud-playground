package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.dependency.DependencyNames;
import com.microsoft.azure.springcloudplayground.service.Annotation;

import java.util.Arrays;

public class KeyVaultModule extends Module {

    public KeyVaultModule() {
        super(ModuleNames.AZURE_KEY_VAULT);
        this.getDependencies().addAll(Arrays.asList(
                DependencyNames.CLOUD_EUREKA_CLIENT,
                DependencyNames.CLOUD_CONFIG_CLIENT,
                DependencyNames.AZURE_KEY_VAULT,
                DependencyNames.WEB
        ));

        this.annotations.add(Annotation.ENABLE_DISCOVERY_CLIENT);
    }
}
