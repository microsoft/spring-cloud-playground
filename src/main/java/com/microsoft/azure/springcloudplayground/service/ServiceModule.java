package com.microsoft.azure.springcloudplayground.service;

import com.microsoft.azure.springcloudplayground.metadata.Describable;
import com.microsoft.azure.springcloudplayground.metadata.MetadataElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.*;

@NoArgsConstructor
public class ServiceModule extends MetadataElement implements Describable {

    @Setter
    private String description;

    @Getter
    private String portName;

    @Getter
    private Integer defaultPort = 0;

    private static final Map<String, Integer> SERVICE_TO_PORT;

    static {
        Map<String, Integer> map = new HashMap<>();

        map.put(ServiceNames.CLOUD_CONFIG_SERVER, 8888);
        map.put(ServiceNames.CLOUD_EUREKA_SERVER, 8761);
        map.put(ServiceNames.CLOUD_GATEWAY, 9999);
        map.put(ServiceNames.CLOUD_HYSTRIX_DASHBOARD, 7979);

        SERVICE_TO_PORT = Collections.unmodifiableMap(map);
    }

    public String getDescription() {
        return this.description;
    }

    public ServiceModule(String id, String name, String description) {
        super(id, name);
        this.description = description;
        this.portName = toPortName(id);
        this.defaultPort = getDefaultPort(id);
    }

    private String toPortName(@NonNull String id) {
        final List<String> tails = new ArrayList<>(Arrays.asList(id.split("-")));
        final String head = tails.remove(0);
        final String tail = String.join("", tails.stream().map(StringUtils::capitalize).toArray(String[]::new));

        return head + tail + "Port";
    }

    @Override
    public void setId(@NonNull String id) {
        this.id = id;
        this.portName = toPortName(id);
        this.defaultPort = getDefaultPort(id);
    }

    private Integer getDefaultPort(@NonNull String id) {
        final Integer port = SERVICE_TO_PORT.get(id);

        return port == null ? 0 : port;
    }

    @Override
    public String toString() {
        return "ServiceModule{" + "id='" + getId() + '\'' + ", name='" + getName()
                + '\'' + ", description='" + this.getDescription() + '\'' + '}';
    }
}
