package com.microsoft.azure.springcloudplayground.service;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

public class ServiceModuleCapability extends ServiceCapability<List<ServiceModuleGroup>> {
    final List<ServiceModuleGroup> content = new ArrayList<>();

    @JsonIgnore
    private final Map<String, ServiceModule> indexedServiceModules = new LinkedHashMap<>();

    public ServiceModuleCapability() {
        super("services", ServiceCapabilityType.HIERARCHICAL_MULTI_SELECT,
                "Service Modules", "Service modules (comma-separated)");
    }

    @Override
    public List<ServiceModuleGroup> getContent() {
        return this.content;
    }

    /**
     * Return the {@link ServiceModule} with the specified id or {@code null} if no such
     * ServiceModule exists.
     * @param id the ID of the ServiceModule
     * @return the ServiceModule or {@code null}
     */
    public ServiceModule get(String id) {
        return this.indexedServiceModules.get(id);
    }

    /**
     * Return all ServiceModules as a flat collection.
     * @return all dependencies
     */
    public Collection<ServiceModule> getAll() {
        return Collections.unmodifiableCollection(this.indexedServiceModules.values());
    }

    public void validate() {
        index();
    }

    @Override
    public void merge(List<ServiceModuleGroup> otherContent) {
        otherContent.forEach((group) -> {
            if (this.content.stream().noneMatch((it) -> group.getName() != null
                    && group.getName().equals(it.getName()))) {
                this.content.add(group);
            }
        });
        index();
    }

    private void index() {
        this.indexedServiceModules.clear();
        this.content.forEach((group) -> group.content.forEach((serviceModule) -> {
            indexServiceModules(serviceModule.getId(), serviceModule);
        }));
    }

    private void indexServiceModules(String id, ServiceModule serviceModule) {
        ServiceModule existing = this.indexedServiceModules.get(id);
        if (existing != null) {
            throw new IllegalArgumentException(
                    "Could not register " + serviceModule + ", another serviceModule "
                            + "has also the '" + id + "' id " + existing);
        }
        this.indexedServiceModules.put(id, serviceModule);
    }
}
