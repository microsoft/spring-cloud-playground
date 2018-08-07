package com.microsoft.azure.springcloudplayground.metadata;

import com.microsoft.azure.springcloudplayground.service.Service;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServicePropsResolver {
    private static final String NEW_LINE = System.getProperty("line.separator");

    public static String generateBootstrapProps(Service service) {
        List<String> bootstrapPropsFiles = service.getModules().stream()
                .map(module -> module.getBootstapPropsTemplate()).collect(Collectors.toList());

        try{
            return combineAndRemoveDuplicate(bootstrapPropsFiles);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate application properties file.", e);
        }
    }

    public static String generateApplicationProps(Service service) {
        List<String> applicationPropsFiles = service.getModules().stream()
                .map(module -> module.getApplicationPropsTemplate()).collect(Collectors.toList());

        try {
            return combineAndRemoveDuplicate(applicationPropsFiles);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate application properties file.", e);
        }
    }

    private static String combineAndRemoveDuplicate(List<String> resourceFiles) throws IOException{
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        List<String> propLines = new ArrayList<>();
        for (String propFile : resourceFiles) {
            List<String> lines = IOUtils.readLines(resourceLoader.getResource(propFile).getInputStream(), Charset.forName("UTF-8"));
            lines.forEach(line -> {
                if (line.isEmpty() || !propLines.contains(line)) {
                    propLines.add(line);
                }
            });
        }

        return String.join(NEW_LINE, propLines);
    }
}
