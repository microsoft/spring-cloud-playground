package com.microsoft.azure.springcloudplayground.metadata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class MetadataElement {

    /**
     * A visual representation of this element.
     */
    @Setter
    protected String name;

    /**
     * The unique id of this element for a given capability.
     */
    @Getter
    @Setter
    protected String id;

    public MetadataElement(MetadataElement other) {
        this(other.id, other.name);
    }

    public MetadataElement(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return this.name != null ? this.name : this.id;
    }
}
