package com.microsoft.azure.springcloudplayground.dependency;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DependencyGroup {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    @JsonIgnore
    private String versionRange;

    @Getter
    @Setter
    @JsonIgnore
    private String bom;

    @Getter
    @Setter
    @JsonIgnore
    private String repository;

    @Getter
    final List<Dependency> content = new ArrayList<>();

    /**
     * Create a new {@link DependencyGroup} instance with the given name.
     * @param name the name of the group
     * @return a new {@link DependencyGroup} instance
     */
    public static DependencyGroup create(String name) {
        DependencyGroup group = new DependencyGroup();
        group.setName(name);
        return group;
    }
}
