package com.microsoft.azure.springcloudplayground.metadata;

public class SimpleGeneratorMetadataProvider implements GeneratorMetadataProvider {

    private final GeneratorMetadata metadata;

    public SimpleGeneratorMetadataProvider(GeneratorMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public GeneratorMetadata get() {
        return this.metadata;
    }

}
