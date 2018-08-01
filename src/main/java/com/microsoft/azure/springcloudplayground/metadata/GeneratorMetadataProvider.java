package com.microsoft.azure.springcloudplayground.metadata;

public interface GeneratorMetadataProvider {

    /**
     * Return the metadata to use. Rather than keeping a handle to a particular instance,
     * implementations may decide to refresh or recompute the metadata if necessary.
     * @return the Generator metadata
     */
    GeneratorMetadata get();

}

