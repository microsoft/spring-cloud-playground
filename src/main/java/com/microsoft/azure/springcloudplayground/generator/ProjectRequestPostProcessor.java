package com.microsoft.azure.springcloudplayground.generator;

import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadata;

public interface ProjectRequestPostProcessor {

    /**
     * Apply this post processor to the given {@code ProjectRequest} <i>before</i> it gets
     * resolved against the specified {@code GeneratorMetadata}.
     * <p>
     * Consider using this hook to customize basic settings of the {@code request}; for
     * more advanced logic (in particular with regards to dependencies), consider using
     * {@code postProcessAfterResolution}.
     * @param request an unresolved {@link ProjectRequest}
     * @param metadata the metadata to use to resolve this request
     * @see ProjectRequest#resolve(GeneratorMetadata)
     */
    default void postProcessBeforeResolution(ProjectRequest request,
                                             GeneratorMetadata metadata) {
    }

    /**
     * Apply this post processor to the given {@code ProjectRequest} <i>after</i> it has
     * been resolved against the specified {@code GeneratorMetadata}.
     * <p>
     * Dependencies, repositories, bills of materials, default properties and others
     * aspects of the request will have been resolved prior to invocation. In particular,
     * note that no further validation checks will be performed.
     * @param request an resolved {@code ProjectRequest}
     * @param metadata the metadata that were used to resolve this request
     */
    default void postProcessAfterResolution(ProjectRequest request,
                                            GeneratorMetadata metadata) {
    }

}
