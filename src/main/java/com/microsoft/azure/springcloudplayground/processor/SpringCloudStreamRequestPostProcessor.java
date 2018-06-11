package com.microsoft.azure.springcloudplayground.processor;

import com.microsoft.azure.springcloudplayground.generator.ProjectRequest;
import com.microsoft.azure.springcloudplayground.dependency.Dependency;
import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadata;
import org.springframework.stereotype.Component;

@Component
class SpringCloudStreamRequestPostProcessor extends AbstractProjectRequestPostProcessor {

    static final Dependency KAFKA_BINDER = Dependency.withId("cloud-stream-binder-kafka",
            "org.springframework.cloud", "spring-cloud-stream-binder-kafka");

    static final Dependency KAFKA_STREAMS_BINDER = Dependency.withId(
            "cloud-stream-binder-kafka-streams", "org.springframework.cloud",
            "spring-cloud-stream-binder-kafka-streams");

    static final Dependency RABBIT_BINDER = Dependency.withId(
            "cloud-stream-binder-rabbit", "org.springframework.cloud",
            "spring-cloud-stream-binder-rabbit");

    static final Dependency SCS_TEST = Dependency.withId("cloud-stream-test",
            "org.springframework.cloud", "spring-cloud-stream-test-support", null,
            Dependency.SCOPE_TEST);

    @Override
    public void postProcessAfterResolution(ProjectRequest request,
                                           GeneratorMetadata metadata) {
        boolean hasSpringCloudStream = hasDependency(request, "cloud-stream");
        boolean hasReactiveSpringCloudStream = hasDependency(request,
                "reactive-cloud-stream");
        boolean hasSpringCloudBus = hasDependency(request, "cloud-bus");
        boolean hasSpringCloudTurbineStream = hasDependency(request,
                "cloud-turbine-stream");
        if (hasSpringCloudStream || hasReactiveSpringCloudStream || hasSpringCloudBus
                || hasSpringCloudTurbineStream) {
            if (hasDependencies(request, "amqp")) {
                request.getResolvedDependencies().add(RABBIT_BINDER);
            }
            if (hasDependencies(request, "kafka")) {
                request.getResolvedDependencies().add(KAFKA_BINDER);
            }
        }
        // Spring Cloud Stream specific
        if (hasSpringCloudStream || hasReactiveSpringCloudStream) {
            if (hasDependencies(request, "kafka-streams")) {
                request.getResolvedDependencies().add(KAFKA_STREAMS_BINDER);
            }
            request.getResolvedDependencies().add(SCS_TEST);
        }
    }

}
