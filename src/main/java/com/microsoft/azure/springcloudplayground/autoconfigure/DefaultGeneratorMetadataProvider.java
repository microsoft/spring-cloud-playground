package com.microsoft.azure.springcloudplayground.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.springcloudplayground.metadata.DefaultMetadataElement;
import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadata;
import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class DefaultGeneratorMetadataProvider implements GeneratorMetadataProvider {

    private static final Logger log = LoggerFactory
            .getLogger(DefaultGeneratorMetadataProvider.class);

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
        List<DefaultMetadataElement> bootVersions = fetchBootVersions();
        if (bootVersions != null && !bootVersions.isEmpty()) {
            if (bootVersions.stream().noneMatch(DefaultMetadataElement::isDefault)) {
                // No default specified
                bootVersions.get(0).setDefault(true);
            }
            metadata.updateSpringBootVersions(bootVersions);
        }
    }

    protected List<DefaultMetadataElement> fetchBootVersions() {
        String url = this.metadata.getConfiguration().getEnv().getSpringBootMetadataUrl();
        if (StringUtils.hasText(url)) {
            try {
                log.info("Fetching boot metadata from {}", url);
                return new SpringBootMetadataReader(this.objectMapper, this.restTemplate,
                        url).getBootVersions();
            }
            catch (Exception e) {
                log.warn("Failed to fetch spring boot metadata", e);
            }
        }
        return null;
    }

}
