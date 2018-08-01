package com.microsoft.azure.springcloudplayground.metadata;

public interface GeneratorMetadataCustomizer {

    /**
     * Customize the {@link GeneratorMetadata}, updating or moving around capabilities
     * before they are validated.
     * @param metadata the Generator metadata
     */
    void customize(GeneratorMetadata metadata);

}
