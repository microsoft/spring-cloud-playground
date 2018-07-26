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

    private String type;

    private String groupId;

    private String artifactId;

    private String version;

    private String bootVersion;

    private String packageName;

    private String javaVersion;

    private String baseDir;

    private String packaging;

    private String description;

    private List<MicroService> microServices;
}
