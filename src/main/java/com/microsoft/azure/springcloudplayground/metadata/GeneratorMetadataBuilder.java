package com.microsoft.azure.springcloudplayground.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class GeneratorMetadataBuilder {

    private final List<GeneratorMetadataCustomizer> customizers = new ArrayList<>();

    private final GeneratorConfiguration configuration;

    private GeneratorMetadataBuilder(GeneratorConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Add a {@link GeneratorProperties} to be merged with other content. Merges the
     * settings only and not the configuration.
     * @param properties the properties to use
     * @return this instance
     * @see #withGeneratorProperties(GeneratorProperties, boolean)
     */
    public GeneratorMetadataBuilder withGeneratorProperties(
            GeneratorProperties properties) {
        return withGeneratorProperties(properties, false);
    }

    /**
     * Add a {@link GeneratorProperties} to be merged with other content.
     * @param properties the settings to merge onto this instance
     * @param mergeConfiguration specify if service configuration should be merged as well
     * @return this instance
     */
    public GeneratorMetadataBuilder withGeneratorProperties(
            GeneratorProperties properties, boolean mergeConfiguration) {
        if (mergeConfiguration) {
            this.configuration.merge(properties);
        }
        return withCustomizer(new InitializerPropertiesCustomizer(properties));
    }

    /**
     * Add a {@link GeneratorMetadata} to be merged with other content.
     * @param resource a resource to a json document describing the metadata to include
     * @return this instance
     */
    public GeneratorMetadataBuilder withGeneratorMetadata(Resource resource) {
        return withCustomizer(new ResourceGeneratorMetadataCustomizer(resource));
    }

    /**
     * Add a {@link GeneratorMetadataCustomizer}. customizers are invoked in their order
     * of addition.
     * @param customizer the customizer to add
     * @return this instance
     * @see GeneratorMetadataCustomizer
     */
    public GeneratorMetadataBuilder withCustomizer(
            GeneratorMetadataCustomizer customizer) {
        this.customizers.add(customizer);
        return this;
    }

    /**
     * Build a {@link GeneratorMetadata} based on the state of this builder.
     * @return a new {@link GeneratorMetadata} instance
     */
    public GeneratorMetadata build() {
        GeneratorConfiguration config = this.configuration != null ? this.configuration
                : new GeneratorConfiguration();
        GeneratorMetadata metadata = createInstance(config);
        for (GeneratorMetadataCustomizer customizer : this.customizers) {
            customizer.customize(metadata);
        }
        applyDefaults(metadata);
        metadata.validate();
        return metadata;
    }

    /**
     * Creates an empty instance based on the specified {@link GeneratorConfiguration}.
     * @param configuration the configuration
     * @return a new {@link GeneratorMetadata} instance
     */
    protected GeneratorMetadata createInstance(GeneratorConfiguration configuration) {
        return new GeneratorMetadata(configuration);
    }

    /**
     * Apply defaults to capabilities that have no value.
     * @param metadata the project generator metadata
     */
    protected void applyDefaults(GeneratorMetadata metadata) {
        if (!StringUtils.hasText(metadata.getName().getContent())) {
            metadata.getName().setContent("demo");
        }
        if (!StringUtils.hasText(metadata.getDescription().getContent())) {
            metadata.getDescription().setContent("Demo project for Spring Boot");
        }
        if (!StringUtils.hasText(metadata.getGroupId().getContent())) {
            metadata.getGroupId().setContent("com.example");
        }
        if (!StringUtils.hasText(metadata.getVersion().getContent())) {
            metadata.getVersion().setContent("0.0.1-SNAPSHOT");
        }
    }

    /**
     * Create a builder instance from the specified {@link GeneratorProperties}.
     * Initialize the configuration to use.
     * @param configuration the configuration to use
     * @return a new {@link GeneratorMetadataBuilder} instance
     * @see #withGeneratorProperties(GeneratorProperties)
     */
    public static GeneratorMetadataBuilder fromGeneratorProperties(
            GeneratorProperties configuration) {
        return new GeneratorMetadataBuilder(configuration)
                .withGeneratorProperties(configuration);
    }

    /**
     * Create an empty builder instance with a default {@link GeneratorConfiguration}.
     * @return a new {@link GeneratorMetadataBuilder} instance
     */
    public static GeneratorMetadataBuilder create() {
        return new GeneratorMetadataBuilder(new GeneratorConfiguration());
    }

    private static class InitializerPropertiesCustomizer
            implements GeneratorMetadataCustomizer {

        private final GeneratorProperties properties;

        InitializerPropertiesCustomizer(GeneratorProperties properties) {
            this.properties = properties;
        }

        @Override
        public void customize(GeneratorMetadata metadata) {
            metadata.getDependencies().merge(this.properties.getDependencies());
            metadata.getServices().merge(this.properties.getServices());
            metadata.getTypes().merge(this.properties.getTypes());
            metadata.getBootVersions().merge(this.properties.getBootVersions());
            metadata.getPackagings().merge(this.properties.getPackagings());
            metadata.getJavaVersions().merge(this.properties.getJavaVersions());
            metadata.getLanguages().merge(this.properties.getLanguages());
            this.properties.getGroupId().apply(metadata.getGroupId());
            this.properties.getArtifactId().apply(metadata.getArtifactId());
            this.properties.getVersion().apply(metadata.getVersion());
            this.properties.getName().apply(metadata.getName());
            this.properties.getDescription().apply(metadata.getDescription());
            this.properties.getPackageName().apply(metadata.getPackageName());
        }

    }

    private static class ResourceGeneratorMetadataCustomizer
            implements GeneratorMetadataCustomizer {

        private static final Logger log = LoggerFactory.getLogger(
                GeneratorMetadataBuilder.ResourceGeneratorMetadataCustomizer.class);

        private static final Charset UTF_8 = Charset.forName("UTF-8");

        private final Resource resource;

        ResourceGeneratorMetadataCustomizer(Resource resource) {
            this.resource = resource;
        }

        @Override
        public void customize(GeneratorMetadata metadata) {
            log.info("Loading project generator  metadata from " + this.resource);
            try {
                String content = StreamUtils.copyToString(this.resource.getInputStream(),
                        UTF_8);
                ObjectMapper objectMapper = new ObjectMapper();
                GeneratorMetadata anotherMetadata = objectMapper.readValue(content,
                        GeneratorMetadata.class);
                metadata.merge(anotherMetadata);
            }
            catch (Exception e) {
                throw new IllegalStateException("Cannot merge", e);
            }
        }

    }

}
