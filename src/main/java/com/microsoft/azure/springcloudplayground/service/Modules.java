package com.microsoft.azure.springcloudplayground.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Modules {

    public static final String CLOUD_GATEWAY = "cloud-gateway";
    public static final String CLOUD_CONFIG_SERVER = "cloud-config-server";
    public static final String CLOUD_EUREKA_SERVER = "cloud-eureka-server";
    public static final String CLOUD_HYSTRIX_DASHBOARD = "cloud-hystrix-dashboard";
    public static final String AZURE_CACHE = "azure-redis-cache";
    public static final String AZURE_STORAGE = "azure-storage";
    public static final String AZURE_SQL_SERVER = "azure-sql-server";
    public static final String AZURE_EVNET_HUB_BINDER = "azure-eventhub-binder";
}
