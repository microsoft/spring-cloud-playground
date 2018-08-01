package com.microsoft.azure.springcloudplayground.processor;

import com.microsoft.azure.springcloudplayground.generator.ProjectRequest;
import com.microsoft.azure.springcloudplayground.generator.ProjectRequestPostProcessor;
import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadata;
import com.microsoft.azure.springcloudplayground.service.Service;
import com.microsoft.azure.springcloudplayground.service.ServiceMetadata;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpringCloudMicroServicePostprocessor extends AbstractProjectRequestPostProcessor implements ProjectRequestPostProcessor {

    @Override
    public void postProcessBeforeResolution(ProjectRequest request, GeneratorMetadata metadata) {
        // Parent pom need these dependencies to determine whether include bom
        if(request.getParent() == null) {
            List<String> dependencies = request.getServices().stream().map(ServiceMetadata::getService).map
                    (Service::getDependencies).flatMap(List::stream).collect(Collectors.toList());
            request.setDependencies(dependencies);
        }
    }

    @Override
    public void postProcessAfterResolution(ProjectRequest request, GeneratorMetadata metadata) {

        for (String serviceName : request.getServices()) {

            ProjectRequest subModule = new ProjectRequest(request);

            Service service = ServiceMetadata.getService(serviceName);
            subModule.setName(serviceName);
            subModule.setArtifactId(request.getArtifactId() + "." + serviceName);
            subModule.setDependencies(service.getDependencies());
            subModule.setBaseDir(serviceName);
            subModule.resolve(metadata);
            if(subModule.getPort() == null) {
                subModule.setPort(service.getPort());
            }

            request.addModule(subModule);
        }
    }
}
