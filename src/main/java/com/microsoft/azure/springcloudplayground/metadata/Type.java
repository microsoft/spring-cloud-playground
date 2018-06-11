package com.microsoft.azure.springcloudplayground.metadata;

import java.util.LinkedHashMap;
import java.util.Map;

public class Type extends DefaultMetadataElement implements Describable {

    private String description;

    @Deprecated
    private String stsId;

    private String action;

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

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStsId() {
        return this.stsId;
    }

    public void setStsId(String stsId) {
        this.stsId = stsId;
    }

    public String getAction() {
        return this.action;
    }

    public Map<String, String> getTags() {
        return this.tags;
    }

}
