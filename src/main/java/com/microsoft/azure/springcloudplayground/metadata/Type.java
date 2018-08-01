package com.microsoft.azure.springcloudplayground.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

public class Type extends DefaultMetadataElement implements Describable {

    @Setter
    private String description;

    @Getter
    @Setter
    @Deprecated
    private String stsId;

    @Getter
    private String action;

    @Getter
    private final Map<String, String> tags = new LinkedHashMap<>();

    public void setAction(String action) {
        String actionToUse = action;
        if (!actionToUse.startsWith("/")) {
            actionToUse = "/" + actionToUse;
        }
        this.action = actionToUse;
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
