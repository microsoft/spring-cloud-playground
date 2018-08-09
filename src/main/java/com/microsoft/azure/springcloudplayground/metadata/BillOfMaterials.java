package com.microsoft.azure.springcloudplayground.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.microsoft.azure.springcloudplayground.exception.InvalidGeneratorMetadataException;
import com.microsoft.azure.springcloudplayground.exception.InvalidVersionException;
import com.microsoft.azure.springcloudplayground.util.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class BillOfMaterials {

    @Getter
    @Setter
    private String groupId;

    @Getter
    @Setter
    private String artifactId;

    @Getter
    @Setter
    private String version;

    @Getter
    private VersionProperty versionProperty;

    @Getter
    @Setter
    private Integer order = Integer.MAX_VALUE;

    @Getter
    @Setter
    private List<String> additionalBoms = new ArrayList<>();

    @Getter
    @Setter
    private List<String> repositories = new ArrayList<>();

    @Getter
    private final List<Mapping> mappings = new ArrayList<>();

    private BillOfMaterials(String groupId, String artifactId) {
        this(groupId, artifactId, null);
    }

    private BillOfMaterials(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public void setVersionProperty(VersionProperty versionProperty) {
        this.versionProperty = versionProperty;
    }

    public void setVersionProperty(String versionPropertyName) {
        this.setVersionProperty(new VersionProperty(versionPropertyName));
    }

    public void validate() {
        if (this.version == null && this.mappings.isEmpty()) {
            throw new InvalidGeneratorMetadataException("No version available for " + this);
        }
        updateVersionRange(VersionParser.DEFAULT);
    }

    public void updateVersionRange(VersionParser versionParser) {
        this.mappings.forEach((Mapping it) -> {
            try {
                it.range = versionParser.parseRange(it.versionRange);
            }
            catch (InvalidVersionException ex) {
                throw new InvalidGeneratorMetadataException(
                        "Invalid version range " + it.versionRange + " for " + this, ex);
            }
        });
    }

    /**
     * Resolve this instance according to the specified Spring Boot {@link Version}.
     * Return a {@link BillOfMaterials} instance that holds the version, repositories and
     * additional BOMs to use, if any.
     * @param bootVersion the Spring Boot version
     * @return the bill of materials
     */
    public BillOfMaterials resolve(Version bootVersion) {
        if (this.mappings.isEmpty()) {
            return this;
        }

        for (Mapping mapping : this.mappings) {
            if (mapping.range.match(bootVersion)) {
                BillOfMaterials resolvedBom = new BillOfMaterials(this.groupId, this.artifactId, mapping.version);

                resolvedBom.setVersionProperty(this.versionProperty);
                resolvedBom.setOrder(this.order);
                resolvedBom.repositories.addAll(mapping.repositories.isEmpty() ? repositories : mapping.repositories);
                resolvedBom.additionalBoms.addAll(mapping.additionalBoms.isEmpty() ?
                        additionalBoms : mapping.additionalBoms);

                return resolvedBom;
            }
        }

        throw new IllegalStateException("No suitable mapping was found for " + this + " and version " + bootVersion);
    }

    @Override
    public String toString() {
        return "BillOfMaterials ["
                + (this.groupId != null ? "groupId=" + this.groupId + ", " : "")
                + (this.artifactId != null ? "artifactId=" + this.artifactId + ", " : "")
                + (this.version != null ? "version=" + this.version + ", " : "")
                + (this.versionProperty != null
                ? "versionProperty=" + this.versionProperty + ", " : "")
                + (this.order != null ? "order=" + this.order + ", " : "")
                + (this.additionalBoms != null
                ? "additionalBoms=" + this.additionalBoms + ", " : "")
                + (this.repositories != null ? "repositories=" + this.repositories : "")
                + "]";
    }

    public static BillOfMaterials create(String groupId, String artifactId) {
        return new BillOfMaterials(groupId, artifactId);
    }

    public static BillOfMaterials create(String groupId, String artifactId, String version) {
        return new BillOfMaterials(groupId, artifactId, version);
    }

    /**
     * Mapping information.
     */
    @NoArgsConstructor
    public static class Mapping {

        @Getter
        @Setter
        private String versionRange;

        @Getter
        @Setter
        private String version;

        @Getter
        @Setter
        private List<String> repositories = new ArrayList<>();

        @Getter
        @Setter
        private List<String> additionalBoms = new ArrayList<>();

        @Getter
        @Setter
        @JsonIgnore
        private VersionRange range;

        private Mapping(String range, String version, String... repositories) {
            this.versionRange = range;
            this.version = version;
            this.repositories.addAll(Arrays.asList(repositories));
        }

        public String determineVersionRangeRequirement() {
            return this.range.toString();
        }

        public static Mapping create(String range, String version) {
            return new Mapping(range, version);
        }

        public static Mapping create(String range, String version,
                                     String... repositories) {
            return new Mapping(range, version, repositories);
        }

        @Override
        public String toString() {
            return "Mapping ["
                    + (this.versionRange != null
                    ? "versionRange=" + this.versionRange + ", " : "")
                    + (this.version != null ? "version=" + this.version + ", " : "")
                    + (this.repositories != null
                    ? "repositories=" + this.repositories + ", " : "")
                    + (this.additionalBoms != null
                    ? "additionalBoms=" + this.additionalBoms + ", " : "")
                    + (this.range != null ? "range=" + this.range : "") + "]";
        }

    }
}
