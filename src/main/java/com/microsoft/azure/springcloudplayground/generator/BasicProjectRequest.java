package com.microsoft.azure.springcloudplayground.generator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class BasicProjectRequest {

    @Getter
    @Setter
    private List<String> style = new ArrayList<>();

    @Getter
    @Setter
    private List<String> dependencies = new ArrayList<>();

    @Getter
    @Setter
    private String name;

    @Setter
    private String type;

    @Setter
    private String description;

    @Setter
    private String groupId;

    @Getter
    @Setter
    private String artifactId;

    @Setter
    private String version;

    @Setter
    private String bootVersion;

    @Setter
    private String packaging;

    @Getter
    @Setter
    private String applicationName;

    @Setter
    private String language;

    @Setter
    private String packageName;

    @Getter
    @Setter
    private String javaVersion;

    // The base directory to create in the archive - no baseDir by default
    @Getter
    @Setter
    private String baseDir;

    @Getter
    private BasicProjectRequest parent;

    public BasicProjectRequest(BasicProjectRequest parentProject) {
        this.parent = parentProject;
    }

    public String getType() {
        if (this.parent != null) {
            return this.parent.getType();
        }

        return this.type;
    }

    public String getDescription() {
        if (this.parent != null) {
            return this.parent.getDescription();
        }

        return this.description;
    }

    public String getGroupId() {
        if (this.parent != null) {
            return this.parent.getGroupId();
        }

        return this.groupId;
    }

    public String getVersion() {
        if (this.parent != null) {
            return this.parent.getVersion();
        }

        return this.version;
    }

    public String getBootVersion() {
        if (this.parent != null) {
            return this.parent.getBootVersion();
        }

        return this.bootVersion;
    }

    public String getPackaging() {
        if (this.parent != null) {
            return this.parent.getPackaging();
        }

        return this.packaging;
    }

    public String getLanguage() {
        if (this.parent != null) {
            return this.parent.getLanguage();
        }

        return this.language;
    }

    public String getPackageName() {
        if (StringUtils.hasText(this.packageName)) {
            return this.packageName;
        } else if (StringUtils.hasText(this.groupId) && StringUtils.hasText(this.artifactId)) {
            return getGroupId() + "." + getArtifactId();
        }

        return null;
    }
}
