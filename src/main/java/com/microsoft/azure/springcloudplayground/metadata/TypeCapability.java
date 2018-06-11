package com.microsoft.azure.springcloudplayground.metadata;

import com.microsoft.azure.springcloudplayground.service.ServiceCapability;
import com.microsoft.azure.springcloudplayground.service.ServiceCapabilityType;

import java.util.ArrayList;
import java.util.List;

public class TypeCapability extends ServiceCapability<List<Type>>
        implements Defaultable<Type> {

    private final List<Type> content = new ArrayList<>();

    public TypeCapability() {
        super("type", ServiceCapabilityType.ACTION, "Type", "project type");
    }

    @Override
    public List<Type> getContent() {
        return this.content;
    }

    /**
     * Return the {@link Type} with the specified id or {@code null} if no such type
     * exists.
     * @param id the ID to find
     * @return the Type or {@code null}
     */
    public Type get(String id) {
        return this.content.stream()
                .filter((it) -> id.equals(it.getId()) || id.equals(it.getStsId()))
                .findFirst().orElse(null);
    }

    /**
     * Return the default {@link Type}.
     */
    @Override
    public Type getDefault() {
        return this.content.stream().filter(DefaultMetadataElement::isDefault).findFirst()
                .orElse(null);
    }

    @Override
    public void merge(List<Type> otherContent) {
        otherContent.forEach((it) -> {
            if (get(it.getId()) == null) {
                this.content.add(it);
            }
        });
    }

}
