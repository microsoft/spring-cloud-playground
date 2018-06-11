package com.microsoft.azure.springcloudplayground.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microsoft.azure.springcloudplayground.dependency.DependencyGroup;
import com.microsoft.azure.springcloudplayground.service.ServiceModuleGroup;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "playground")
public class GeneratorProperties extends GeneratorConfiguration {

    /**
     * Dependencies, organized in groups (i.e. themes).
     */
    @JsonIgnore
    private final List<DependencyGroup> dependencies = new ArrayList<>();

    /**
     * ServiceModules, organized in groups (i.e. themes).
     */
    @JsonIgnore
    private final List<ServiceModuleGroup> services = new ArrayList<>();

    /**
     * Available project types.
     */
    @JsonIgnore
    private final List<Type> types = new ArrayList<>();

    /**
     * Available packaging types.
     */
    @JsonIgnore
    private final List<DefaultMetadataElement> packagings = new ArrayList<>();

    /**
     * Available java versions.
     */
    @JsonIgnore
    private final List<DefaultMetadataElement> javaVersions = new ArrayList<>();

    /**
     * Available programming languages.
     */
    @JsonIgnore
    private final List<DefaultMetadataElement> languages = new ArrayList<>();

    /**
     * Available Spring Boot versions.
     */
    @JsonIgnore
    private final List<DefaultMetadataElement> bootVersions = new ArrayList<>();

    /**
     * GroupId metadata.
     */
    @JsonIgnore
    private final SimpleElement groupId = new SimpleElement("com.example");

    /**
     * ArtifactId metadata.
     */
    @JsonIgnore
    private final SimpleElement artifactId = new SimpleElement(null);

    /**
     * Version metadata.
     */
    @JsonIgnore
    private final SimpleElement version = new SimpleElement("0.0.1-SNAPSHOT");

    /**
     * Name metadata.
     */
    @JsonIgnore
    private final SimpleElement name = new SimpleElement("demo");

    /**
     * Description metadata.
     */
    @JsonIgnore
    private final SimpleElement description = new SimpleElement(
            "Demo project for Spring Boot");

    /**
     * Package name metadata.
     */
    @JsonIgnore
    private final SimpleElement packageName = new SimpleElement(null);

    public List<DependencyGroup> getDependencies() {
        return this.dependencies;
    }

    public List<ServiceModuleGroup> getServices() {
        return this.services;
    }

    public List<Type> getTypes() {
        return this.types;
    }

    public List<DefaultMetadataElement> getPackagings() {
        return this.packagings;
    }

    public List<DefaultMetadataElement> getJavaVersions() {
        return this.javaVersions;
    }

    public List<DefaultMetadataElement> getLanguages() {
        return this.languages;
    }

    public List<DefaultMetadataElement> getBootVersions() {
        return this.bootVersions;
    }

    public SimpleElement getGroupId() {
        return this.groupId;
    }

    public SimpleElement getArtifactId() {
        return this.artifactId;
    }

    public SimpleElement getVersion() {
        return this.version;
    }

    public SimpleElement getName() {
        return this.name;
    }

    public SimpleElement getDescription() {
        return this.description;
    }

    public SimpleElement getPackageName() {
        return this.packageName;
    }

    /**
     * A simple element from the properties.
     */
    public static class SimpleElement {

        /**
         * Element title.
         */
        private String title;

        /**
         * Element description.
         */
        private String description;

        /**
         * Element default value.
         */
        private String value;

        public SimpleElement(String value) {
            this.value = value;
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

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void apply(TextCapability capability) {
            if (StringUtils.hasText(this.title)) {
                capability.setTitle(this.title);
            }
            if (StringUtils.hasText(this.description)) {
                capability.setDescription(this.description);
            }
            if (StringUtils.hasText(this.value)) {
                capability.setContent(this.value);
            }
        }

    }
}
