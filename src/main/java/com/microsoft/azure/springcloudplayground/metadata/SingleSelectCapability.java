package com.microsoft.azure.springcloudplayground.metadata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.springcloudplayground.service.ServiceCapability;
import com.microsoft.azure.springcloudplayground.service.ServiceCapabilityType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SingleSelectCapability
        extends ServiceCapability<List<DefaultMetadataElement>>
        implements Defaultable<DefaultMetadataElement> {

    private final List<DefaultMetadataElement> content = new CopyOnWriteArrayList<>();

    @JsonCreator
    SingleSelectCapability(@JsonProperty("id") String id) {
        this(id, null, null);
    }

    public SingleSelectCapability(String id, String title, String description) {
        super(id, ServiceCapabilityType.SINGLE_SELECT, title, description);
    }

    @Override
    public List<DefaultMetadataElement> getContent() {
        return this.content;
    }

    /**
     * Return the default element of this capability.
     */
    @Override
    public DefaultMetadataElement getDefault() {
        return this.content.stream().filter(DefaultMetadataElement::isDefault).findFirst()
                .orElse(null);
    }

    /**
     * Return the element with the specified id or {@code null} if no such element exists.
     * @param id the ID of the element to find
     * @return the element or {@code null}
     */
    public DefaultMetadataElement get(String id) {
        return this.content.stream().filter((it) -> id.equals(it.getId())).findFirst()
                .orElse(null);
    }

    @Override
    public void merge(List<DefaultMetadataElement> otherContent) {
        otherContent.forEach((it) -> {
            if (get(it.getId()) == null) {
                this.content.add(it);
            }
        });
    }

}
