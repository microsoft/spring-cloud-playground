package com.microsoft.azure.springcloudplayground.metadata;

public class DefaultMetadataElement extends MetadataElement {

    private boolean defaultValue;

    public DefaultMetadataElement() {
    }

    public DefaultMetadataElement(String id, String name, boolean defaultValue) {
        super(id, name);
        this.defaultValue = defaultValue;
    }

    public DefaultMetadataElement(String id, boolean defaultValue) {
        this(id, null, defaultValue);
    }

    public void setDefault(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isDefault() {
        return this.defaultValue;
    }

    public static DefaultMetadataElement create(String id, boolean defaultValue) {
        return new DefaultMetadataElement(id, defaultValue);
    }

    public static DefaultMetadataElement create(String id, String name,
                                                boolean defaultValue) {
        return new DefaultMetadataElement(id, name, defaultValue);
    }

}
