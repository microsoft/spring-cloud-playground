package com.microsoft.azure.springcloudplayground.dependency;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microsoft.azure.springcloudplayground.service.ServiceCapability;
import com.microsoft.azure.springcloudplayground.service.ServiceCapabilityType;
import com.microsoft.azure.springcloudplayground.util.VersionParser;

import java.util.*;

public class DependenciesCapability extends ServiceCapability<List<DependencyGroup>> {

    final List<DependencyGroup> content = new ArrayList<>();

    @JsonIgnore
    private final Map<String, Dependency> indexedDependencies = new LinkedHashMap<>();

    public DependenciesCapability() {
        super("dependencies", ServiceCapabilityType.HIERARCHICAL_MULTI_SELECT,
                "Project dependencies", "dependency identifiers (comma-separated)");
    }

    @Override
    public List<DependencyGroup> getContent() {
        return this.content;
    }

    /**
     * Return the {@link Dependency} with the specified id or {@code null} if no such
     * dependency exists.
     * @param id the ID of the dependency
     * @return the dependency or {@code null}
     */
    public Dependency get(String id) {
        return this.indexedDependencies.get(id);
    }

    /**
     * Return all dependencies as a flat collection.
     * @return all dependencies
     */
    public Collection<Dependency> getAll() {
        return Collections.unmodifiableCollection(this.indexedDependencies.values());
    }

    public void validate() {
        index();
    }

    public void updateVersionRange(VersionParser versionParser) {
        this.indexedDependencies.values()
                .forEach((it) -> it.updateVersionRanges(versionParser));
    }

    @Override
    public void merge(List<DependencyGroup> otherContent) {
        otherContent.forEach((group) -> {
            if (this.content.stream().noneMatch((it) -> group.getName() != null
                    && group.getName().equals(it.getName()))) {
                this.content.add(group);
            }
        });
        index();
    }

    private void index() {
        this.indexedDependencies.clear();
        this.content.forEach((group) -> group.content.forEach((dependency) -> {
            // Apply defaults
            if (dependency.getVersionRange() == null && group.getVersionRange() != null) {
                dependency.setVersionRange(group.getVersionRange());
            }
            if (dependency.getBom() == null && group.getBom() != null) {
                dependency.setBom(group.getBom());
            }
            if (dependency.getRepository() == null && group.getRepository() != null) {
                dependency.setRepository(group.getRepository());
            }

            dependency.resolve();
            indexDependency(dependency.getId(), dependency);
            for (String alias : dependency.getAliases()) {
                indexDependency(alias, dependency);
            }
        }));
    }

    private void indexDependency(String id, Dependency dependency) {
        Dependency existing = this.indexedDependencies.get(id);
        if (existing != null) {
            throw new IllegalArgumentException(
                    "Could not register " + dependency + " another dependency "
                            + "has also the '" + id + "' id " + existing);
        }
        this.indexedDependencies.put(id, dependency);
    }

}
