package com.microsoft.azure.springcloudplayground.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.springcloudplayground.metadata.DefaultMetadataElement;
import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadata;
import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadataProvider;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public class DefaultGeneratorMetadataProvider implements GeneratorMetadataProvider {

    private static final List<DefaultMetadataElement> bootVersions = Arrays.asList(
            DefaultMetadataElement.create("2.1.0.BUILD-SNAPSHOT", "2.1.0 (SNAPSHOT)", false),
            DefaultMetadataElement.create("2.0.4.BUILD-SNAPSHOT", "2.0.4 (SNAPSHOT)", false),
            DefaultMetadataElement.create("2.0.3.RELEASE", "2.0.3", true),
            DefaultMetadataElement.create("1.5.15.BUILD-SNAPSHOT", "1.5.15 (SNAPSHOT)", false),
            DefaultMetadataElement.create("1.5.14.RELEASE", "1.5.14", false)
    );

    private final GeneratorMetadata metadata;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    public DefaultGeneratorMetadataProvider(GeneratorMetadata metadata,
                                            ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.metadata = metadata;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    @Cacheable(value = "generator.metadata", key = "'metadata'")
    public GeneratorMetadata get() {
        updateGeneratorMetadata(this.metadata);
        return this.metadata;
    }

    protected void updateGeneratorMetadata(GeneratorMetadata metadata) {
        if (bootVersions.stream().noneMatch(DefaultMetadataElement::isDefault)) {
            // No default specified
            bootVersions.get(0).setDefault(true);
        }

        metadata.updateSpringBootVersions(bootVersions);
    }

// Keep following method in case the data changes.
//    protected List<DefaultMetadataElement> fetchBootVersions() {
//        String url = this.metadata.getConfiguration().getEnv().getSpringBootMetadataUrl();
//        if (StringUtils.hasText(url)) {
//            try {
//                log.info("Fetching boot metadata from {}", url);
//                return new SpringBootMetadataReader(this.objectMapper, this.restTemplate, url).getBootVersions();
//            }
//            catch (Exception e) {
//                log.warn("Failed to fetch spring boot metadata", e);
//            }
//        }
//        return null;
//    }
}
