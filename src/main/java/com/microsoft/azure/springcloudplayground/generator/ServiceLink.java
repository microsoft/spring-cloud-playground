package com.microsoft.azure.springcloudplayground.generator;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceLink {
    private String serviceName;
    private String serviceUrl;
    private boolean isBasic;
}

