package com.microsoft.azure.springcloudplayground.processor;

import com.microsoft.azure.springcloudplayground.generator.ProjectRequest;
import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadata;
import com.microsoft.azure.springcloudplayground.util.Version;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
class SpringBoot2RequestPostProcessor extends AbstractProjectRequestPostProcessor {

    private static final Version VERSION_2_0_0_M1 = Version.parse("2.0.0.M1");

    private static final List<String> VALID_VERSIONS = Arrays.asList("1.8", "9", "10");

    @Override
    public void postProcessAfterResolution(ProjectRequest request,
                                           GeneratorMetadata metadata) {
        if (!VALID_VERSIONS.contains(request.getJavaVersion())
                && isSpringBootVersionAtLeastAfter(request, VERSION_2_0_0_M1)) {
            request.setJavaVersion("1.8");
        }
    }

}
