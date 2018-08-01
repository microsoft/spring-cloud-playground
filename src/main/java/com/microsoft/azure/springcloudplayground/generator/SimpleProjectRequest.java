package com.microsoft.azure.springcloudplayground.generator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SimpleProjectRequest {

    private String name;

    private String type = "maven-project";

    private String groupId;

    private String artifactId;

    private String version = "0.0.1-SNAPSHOT";

    private String bootVersion = "2.0.3.RELEASE";

    private String packageName;

    private String javaVersion = "1.8";

    private String baseDir;

    private String packaging = "pom";

    private String description = "Project for spring cloud on azure";

    private List<MicroService> microServices;
}
