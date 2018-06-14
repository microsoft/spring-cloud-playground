package com.microsoft.azure.springcloudplayground.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microsoft.azure.springcloudplayground.dependency.DependencyGroup;
import com.microsoft.azure.springcloudplayground.service.ServiceModuleGroup;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "playground")
public class GeneratorProperties extends GeneratorConfiguration {

    /**
     * Dependencies, organized in groups (i.e. themes).
     */
    @Getter
    @JsonIgnore
    private final List<DependencyGroup> dependencies = new ArrayList<>();

    /**
     * ServiceModules, organized in groups (i.e. themes).
     */
    @Getter
    @JsonIgnore
    private final List<ServiceModuleGroup> services = new ArrayList<>();

    /**
     * Available project types.
     */
    @Getter
    @JsonIgnore
    private final List<Type> types = new ArrayList<>();

    /**
     * Available packaging types.
     */
    @Getter
    @JsonIgnore
    private final List<DefaultMetadataElement> packagings = new ArrayList<>();

    /**
     * Available java versions.
     */
    @Getter
    @JsonIgnore
    private final List<DefaultMetadataElement> javaVersions = new ArrayList<>();

    /**
     * Available programming languages.
     */
    @Getter
    @JsonIgnore
    private final List<DefaultMetadataElement> languages = new ArrayList<>();

    /**
     * Available Spring Boot versions.
     */
    @Getter
    @JsonIgnore
    private final List<DefaultMetadataElement> bootVersions = new ArrayList<>();

    /**
     * GroupId metadata.
     */
    @Getter
    @JsonIgnore
    private final SimpleElement groupId = new SimpleElement("com.example");

    /**
     * ArtifactId metadata.
     */
    @Getter
    @JsonIgnore
    private final SimpleElement artifactId = new SimpleElement(null);

    /**
     * Version metadata.
     */
    @Getter
    @JsonIgnore
    private final SimpleElement version = new SimpleElement("0.0.1-SNAPSHOT");

    /**
     * Name metadata.
     */
    @Getter
    @JsonIgnore
    private final SimpleElement name = new SimpleElement("demo");

    /**
     * Description metadata.
     */
    @Getter
    @JsonIgnore
    private final SimpleElement description = new SimpleElement(
            "Demo project for Spring Boot");

    /**
     * Package name metadata.
     */
    @Getter
    @JsonIgnore
    private final SimpleElement packageName = new SimpleElement(null);

    /**
     * A simple element from the properties.
     */
    @Getter
    @Setter
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
