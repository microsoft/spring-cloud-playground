package com.microsoft.azure.springcloudplayground.service;

import java.util.ArrayList;
import java.util.List;

public class ServiceModuleGroup {
    private String name;

    final List<ServiceModule> content = new ArrayList<>();

    /**
     * Return the name of this group.
     * @return the name of the group
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the {@link ServiceModule modules} of this group.
     * @return the content
     */
    public List<ServiceModule> getContent() {
        return this.content;
    }

    /**
     * Create a new {@link ServiceModuleGroup} instance with the given name.
     * @param name the name of the group
     * @return a new {@link ServiceModuleGroup} instance
     */
    public static ServiceModuleGroup create(String name) {
        ServiceModuleGroup group = new ServiceModuleGroup();
        group.setName(name);
        return group;
    }
}
