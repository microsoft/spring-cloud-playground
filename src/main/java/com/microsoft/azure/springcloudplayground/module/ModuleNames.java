package com.microsoft.azure.springcloudplayground.module;

import com.microsoft.azure.springcloudplayground.exception.InvalidGeneratorMetadataException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ModuleNames {

    public static final String AZURE = "azure";

    public static final String CLOUD_GATEWAY = "cloud-gateway";

    public static final String CLOUD_CONFIG_SERVER = "cloud-config-server";

    public static final String CLOUD_EUREKA_SERVER = "cloud-eureka-server";

    public static final String CLOUD_HYSTRIX_DASHBOARD = "cloud-hystrix-dashboard";

    public static final String AZURE_CACHE = "azure-redis-cache";

    public static final String AZURE_STORAGE = "azure-storage";

    public static final String AZURE_SQL_SERVER = "azure-sql-server";

    public static final String AZURE_EVNET_HUB_BINDER = "azure-eventhub-binder";

    private static final Map<String, Module> NAME_TO_MODULE;

    static {
        Map<String, Module> map = new HashMap<>();

        map.put(CLOUD_GATEWAY, new GatewayModule());
        map.put(CLOUD_CONFIG_SERVER, new ConfigModule());
        map.put(CLOUD_EUREKA_SERVER, new EurekaModule());
        map.put(CLOUD_HYSTRIX_DASHBOARD, new HystrixDashboardModule());
        map.put(AZURE_CACHE, new CacheModule());
        map.put(AZURE_STORAGE, new StorageModule());
        map.put(AZURE_SQL_SERVER, new SqlServerModule());
        map.put(AZURE_EVNET_HUB_BINDER, new EventHubModule());

        NAME_TO_MODULE = Collections.unmodifiableMap(map);
    }

    public static Module toModule(@NonNull String moduleName) {
        Module module = NAME_TO_MODULE.get(moduleName);

        if (module == null) {
            throw new InvalidGeneratorMetadataException("unknown module name: " + moduleName);
        }

        return module;
    }
}
