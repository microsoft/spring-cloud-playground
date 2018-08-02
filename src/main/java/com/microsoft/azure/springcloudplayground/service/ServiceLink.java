package com.microsoft.azure.springcloudplayground.service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceLink {

    private String serviceName;

    private String serviceUrl;
}

