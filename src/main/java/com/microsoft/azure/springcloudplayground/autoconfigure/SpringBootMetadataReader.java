package com.microsoft.azure.springcloudplayground.autoconfigure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.microsoft.azure.springcloudplayground.metadata.DefaultMetadataElement;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpringBootMetadataReader {

    private final JsonNode content;

    /**
     * Parse the content of the metadata at the specified url.
     * @param objectMapper the object mapper
     * @param restTemplate the rest template
     * @param url the metadata URL
     * @throws IOException on load error
     */
    public SpringBootMetadataReader(ObjectMapper objectMapper, RestTemplate restTemplate,
                                    String url) throws IOException {
        this.content = objectMapper
                .readTree(restTemplate.getForObject(url, String.class));
    }

    /**
     * Return the boot versions parsed by this instance.
     * @return the versions
     */
    public List<DefaultMetadataElement> getBootVersions() {
        ArrayNode array = (ArrayNode) this.content.get("projectReleases");
        List<DefaultMetadataElement> list = new ArrayList<>();
        for (JsonNode it : array) {
            DefaultMetadataElement version = new DefaultMetadataElement();
            version.setId(it.get("version").textValue());
            String name = it.get("versionDisplayName").textValue();
            version.setName(
                    it.get("snapshot").booleanValue() ? name + " (SNAPSHOT)" : name);
            version.setDefault(it.get("current").booleanValue());
            list.add(version);
        }
        return list;
    }

}
