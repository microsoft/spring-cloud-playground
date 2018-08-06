package com.microsoft.azure.springcloudplayground.service;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

public class ServiceMetadataCapability extends ServiceCapability<List<ServiceMetadataGroup>> {

    final List<ServiceMetadataGroup> content = new ArrayList<>();

    @JsonIgnore
    private final Map<String, ServiceMetadata> indexedServiceModules = new LinkedHashMap<>();

    public ServiceMetadataCapability() {
        super("services", ServiceCapabilityType.HIERARCHICAL_MULTI_SELECT,
                "Module ModuleNames", "Module modules (comma-separated)");
    }

    @Override
    public List<ServiceMetadataGroup> getContent() {
        return this.content;
    }

    /**
     * Return the {@link ServiceMetadata} with the specified id or {@code null} if no such
     * ServiceMetadata exists.
     * @param id the ID of the ServiceMetadata
     * @return the ServiceMetadata or {@code null}
     */
    public ServiceMetadata get(String id) {
        return this.indexedServiceModules.get(id);
    }

    /**
     * Return all ServiceModules as a flat collection.
     * @return all dependencies
     */
    public Collection<ServiceMetadata> getAll() {
        return Collections.unmodifiableCollection(this.indexedServiceModules.values());
    }

    public void validate() {
        index();
    }

    @Override
    public void merge(List<ServiceMetadataGroup> otherContent) {
        otherContent.stream().filter(g -> content.stream()
                .noneMatch(c -> g.getName() != null && g.getName().equals(c.getName())))
                .forEach(this.content::add);

        index();
    }

    private void index() {
        this.indexedServiceModules.clear();
        this.content.forEach(group -> group.content.forEach(module -> indexServiceModules(module.getId(), module)));
    }

    private void indexServiceModules(String id, ServiceMetadata serviceMetadata) {
        ServiceMetadata existing = this.indexedServiceModules.get(id);

        if (existing != null) {
            throw new IllegalArgumentException(
                    "Could not register " + serviceMetadata + ", another serviceMetadata "
                            + "has also the '" + id + "' id " + existing);
        }

        this.indexedServiceModules.put(id, serviceMetadata);
    }
}
