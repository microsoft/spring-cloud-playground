package com.microsoft.azure.springcloudplayground.generator;

import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadata;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class ProjectRequestResolver {

    private final List<ProjectRequestPostProcessor> postProcessors;

    public ProjectRequestResolver(List<ProjectRequestPostProcessor> postProcessors) {
        this.postProcessors = new ArrayList<>(postProcessors);
    }

    public ProjectRequest resolve(ProjectRequest request, GeneratorMetadata metadata) {
        Assert.notNull(request, "Request must not be null");
        applyPostProcessBeforeResolution(request, metadata);
        request.resolve(metadata);
        applyPostProcessAfterResolution(request, metadata);
        return request;
    }

    private void applyPostProcessBeforeResolution(ProjectRequest request,
                                                  GeneratorMetadata metadata) {
        for (ProjectRequestPostProcessor processor : this.postProcessors) {
            processor.postProcessBeforeResolution(request, metadata);
        }
    }

    private void applyPostProcessAfterResolution(ProjectRequest request,
                                                 GeneratorMetadata metadata) {
        for (ProjectRequestPostProcessor processor : this.postProcessors) {
            processor.postProcessAfterResolution(request, metadata);
        }
    }

}

