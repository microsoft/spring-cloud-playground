package com.microsoft.azure.springcloudplayground.service;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class ServiceModuleGroup {

    @Getter
    @Setter
    private String name;

    @Getter
    final List<ServiceModule> content = new ArrayList<>();

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
