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
public class ServiceMetadata extends MetadataElement implements Describable {

    @Setter
    private String description;

    @Getter
    private String portName;

    @Getter
    @Setter
    private Integer defaultPort;

    @Getter
    @Setter
    private boolean mandatory;

    public String getDescription() {
        return this.description;
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
    }

    @Override
    public String toString() {
        return "ServiceMetadata{" + "id='" + getId() + '\'' + ", name='" + getName()
                + '\'' + ", description='" + this.getDescription() + '\'' + ", mandatory='" + mandatory
                + '\'' + '}';
    }
}
