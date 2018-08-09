package com.microsoft.azure.springcloudplayground.metadata;

import org.springframework.cache.annotation.Cacheable;

import java.util.Arrays;
import java.util.List;

public class DefaultGeneratorMetadataProvider implements GeneratorMetadataProvider {

    private static final List<DefaultMetadataElement> bootVersions = Arrays.asList(
            DefaultMetadataElement.create("2.1.0.BUILD-SNAPSHOT", "2.1.0 (SNAPSHOT)", false),
            DefaultMetadataElement.create("2.0.4.BUILD-SNAPSHOT", "2.0.4 (SNAPSHOT)", false),
            DefaultMetadataElement.create("2.0.3.RELEASE", "2.0.3", true),
            DefaultMetadataElement.create("1.5.15.BUILD-SNAPSHOT", "1.5.15 (SNAPSHOT)", false),
            DefaultMetadataElement.create("1.5.14.RELEASE", "1.5.14", false)
    );

    private final GeneratorMetadata metadata;

    public DefaultGeneratorMetadataProvider(GeneratorMetadata metadata) {
        this.metadata = metadata;
        updateGeneratorMetadata(this.metadata);
    }

    @Override
    @Cacheable(value = "generator.metadata", key = "'metadata'")
    public GeneratorMetadata get() {
        return this.metadata;
    }

    protected void updateGeneratorMetadata(GeneratorMetadata metadata) {
        if (bootVersions.stream().noneMatch(DefaultMetadataElement::isDefault)) {
            // No default specified
            bootVersions.get(0).setDefault(true);
        }

        metadata.updateSpringBootVersions(bootVersions);
    }
}
