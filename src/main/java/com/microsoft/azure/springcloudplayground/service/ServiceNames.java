package com.microsoft.azure.springcloudplayground.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceNames {

    public static final String CLOUD_GATEWAY = "cloud-gateway";

    public static final String CLOUD_CONFIG_SERVER = "cloud-config-server";

    public static final String CLOUD_EUREKA_SERVER = "cloud-eureka-server";

    public static final String CLOUD_HYSTRIX_DASHBOARD = "cloud-hystrix-dashboard";
}
