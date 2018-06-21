package com.microsoft.azure.springcloudplayground.service;

import com.microsoft.azure.springcloudplayground.metadata.Describable;
import com.microsoft.azure.springcloudplayground.metadata.MetadataElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
public class ServiceModule extends MetadataElement implements Describable {

    @Setter
    private String description;

    @Getter
    private String portId;

    @Override
    public String getDescription() {
        return this.description;
    }

    public ServiceModule(String id, String name, String description) {
        super(id, name);
        this.description = description;
        this.portId = toPortId(id);
    }

    private String toPortId(@NonNull String id) {
        final List<String> tails = new ArrayList<>(Arrays.asList(id.split("-")));
        final String head = tails.remove(0);
        final String tail = String.join("", tails.stream().map(StringUtils::capitalize).toArray(String[]::new));

        return head + tail + "Port";
    }

    @Override
    public void setId(@NonNull String id) {
        this.id = id;
        this.portId = toPortId(id);
    }

    @Override
    public String toString() {
        return "ServiceModule{" + "id='" + getId() + '\'' + ", name='" + getName()
                + '\'' + ", description='" + this.getDescription() + '\'' + '}';
    }
}
