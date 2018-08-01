package com.microsoft.azure.springcloudplayground.service;

import java.util.Arrays;

class SqlServerService extends Service {

    SqlServerService(){
        super(Modules.AZURE_SQL_SERVER, 8083);
        this.getDependencies().addAll(Arrays.asList(Dependencies.CLOUD_EUREKA_CLIENT, Dependencies
                .CLOUD_CONFIG_CLIENT, Dependencies.AZURE_SQL_SERVER, Dependencies.WEB));

        this.annotations.add(Annotation.ENABLE_DISCOVERY_CLIENT);
    }
}
