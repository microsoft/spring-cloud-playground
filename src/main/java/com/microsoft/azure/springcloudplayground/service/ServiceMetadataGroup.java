package com.microsoft.azure.springcloudplayground.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class ServiceMetadataGroup {

    @Getter
    @Setter
    private String name;

    @Getter
    final List<ServiceMetadata> content = new ArrayList<>();
}
