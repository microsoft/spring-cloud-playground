package com.microsoft.azure.springcloudplayground.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@JsonIgnoreProperties({ "default", "all" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class ServiceCapability<T> implements Cloneable {

    private final String id;

    private final ServiceCapabilityType type;

    /**
     * A title of the capability, used as a header text or label.
     */
    private String title;

    /**
     * A description of the capability, used in help usage or UI tooltips.
     */
    private String description;

    protected ServiceCapability(String id, ServiceCapabilityType type, String title,
                                String description) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return this.id;
    }

    public ServiceCapabilityType getType() {
        return this.type;
    }

    /**
     * Return the "content" of this capability. The structure of the content vastly
     * depends on the {@link ServiceCapability type} of the capability.
     * @return the content
     */
    public abstract T getContent();

    /**
     * Merge the content of this instance with the specified content.
     * @param otherContent the content to merge
     * @see #merge(ServiceCapability)
     */
    public abstract void merge(T otherContent);

    /**
     * Merge this capability with the specified argument. The service capabilities should
     * match (i.e have the same {@code id} and {@code type}). Sub-classes may merge
     * additional content.
     * @param other the content to merge
     */
    public void merge(ServiceCapability<T> other) {
        Assert.notNull(other, "Other must not be null");
        Assert.isTrue(this.id.equals(other.id), "Ids must be equals");
        Assert.isTrue(this.type.equals(other.type), "Types must be equals");
        if (StringUtils.hasText(other.title)) {
            this.title = other.title;
        }
        if (StringUtils.hasText(other.description)) {
            this.description = other.description;
        }
        merge(other.getContent());
    }

}
