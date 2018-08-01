package com.microsoft.azure.springcloudplayground.service;

import java.util.Arrays;

class HystrixDashboardService extends Service {

    HystrixDashboardService(int port) {
        super(Modules.CLOUD_HYSTRIX_DASHBOARD, port, "/hystrix");
        this.getDependencies().addAll(Arrays.asList(Dependencies.CLOUD_HYSTRIX_DASHBOARD, Dependencies
                .CLOUD_CONFIG_CLIENT, Dependencies.CLOUD_EUREKA_CLIENT, Dependencies.WEB));
        this.annotations.addAll(Arrays.asList(Annotation.ENABLE_DISCOVERY_CLIENT, Annotation.ENABLE_HYSTRIX_DASHBOARD));
    }

    HystrixDashboardService() {
        this(7979);
    }
}
