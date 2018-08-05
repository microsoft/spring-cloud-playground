package com.microsoft.azure.springcloudplayground.metadata;

import com.microsoft.azure.springcloudplayground.dependency.DependenciesCapability;
import com.microsoft.azure.springcloudplayground.dependency.Dependency;
import com.microsoft.azure.springcloudplayground.exception.InvalidGeneratorMetadataException;
import com.microsoft.azure.springcloudplayground.service.ServiceMetadataCapability;
import com.microsoft.azure.springcloudplayground.util.Version;
import com.microsoft.azure.springcloudplayground.util.VersionParser;
import com.microsoft.azure.springcloudplayground.util.VersionProperty;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GeneratorMetadata {

    @Getter
    private final GeneratorConfiguration configuration;

    @Getter
    private final DependenciesCapability dependencies = new DependenciesCapability();

    @Getter
    private final ServiceMetadataCapability services = new ServiceMetadataCapability();

    @Getter
    private final TypeCapability types = new TypeCapability();

    @Getter
    private final SingleSelectCapability bootVersions = new SingleSelectCapability(
            "bootVersion", "Spring Boot Version", "spring boot version");

    @Getter
    private final SingleSelectCapability packagings = new SingleSelectCapability(
            "packaging", "Packaging", "project packaging");

    @Getter
    private final SingleSelectCapability javaVersions = new SingleSelectCapability(
            "javaVersion", "Java Version", "language level");

    @Getter
    private final SingleSelectCapability languages = new SingleSelectCapability(
            "language", "Language", "programming language");

    @Getter
    private final TextCapability name = new TextCapability("name", "Name",
            "project name (infer application name)");

    @Getter
    private final TextCapability description = new TextCapability("description",
            "Description", "project description");

    @Getter
    private final TextCapability groupId = new TextCapability("groupId", "Group",
            "project coordinates");

    @Getter
    private final TextCapability artifactId = new ArtifactIdCapability(this.name);

    @Getter
    private final TextCapability version = new TextCapability("version", "Version",
            "project version");

    @Getter
    private final TextCapability packageName = new PackageCapability(this.groupId,
            this.artifactId);

    public GeneratorMetadata() {
        this(new GeneratorConfiguration());
    }

    protected GeneratorMetadata(GeneratorConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Merge this instance with the specified argument.
     * @param other the other instance
     */
    public void merge(GeneratorMetadata other) {
        this.configuration.merge(other.configuration);
        this.dependencies.merge(other.dependencies);
        this.services.merge(other.services);
        this.types.merge(other.types);
        this.bootVersions.merge(other.bootVersions);
        this.packagings.merge(other.packagings);
        this.javaVersions.merge(other.javaVersions);
        this.languages.merge(other.languages);
        this.name.merge(other.name);
        this.description.merge(other.description);
        this.groupId.merge(other.groupId);
        this.artifactId.merge(other.artifactId);
        this.version.merge(other.version);
        this.packageName.merge(other.packageName);
    }

    /**
     * Validate the metadata.
     */
    public void validate() {
        this.configuration.validate();
        this.dependencies.validate();
        this.services.validate();

        Map<String, Repository> repositories = this.configuration.getEnv().getRepositories();
        Map<String, BillOfMaterials> boms = this.configuration.getEnv().getBoms();

        for (Dependency dependency : this.dependencies.getAll()) {
            if (dependency.getBom() != null && !boms.containsKey(dependency.getBom())) {
                throw new InvalidGeneratorMetadataException(
                        "Dependency " + dependency + "defines an invalid BOM id "
                                + dependency.getBom() + ", available boms " + boms);
            }

            if (dependency.getRepository() != null && !repositories.containsKey(dependency.getRepository())) {
                throw new InvalidGeneratorMetadataException("Dependency " + dependency
                        + "defines an invalid repository id " + dependency.getRepository()
                        + ", available repositories " + repositories);
            }
        }

        for (BillOfMaterials bom : boms.values()) {
            for (String r : bom.getRepositories()) {
                if (!repositories.containsKey(r)) {
                    throw new InvalidGeneratorMetadataException(
                            bom + "defines an invalid repository id " + r
                                    + ", available repositories " + repositories);
                }
            }

            for (String b : bom.getAdditionalBoms()) {
                if (!boms.containsKey(b)) {
                    throw new InvalidGeneratorMetadataException(
                            bom + " defines an invalid " + "additional bom id " + b
                                    + ", available boms " + boms);
                }
            }

            for (BillOfMaterials.Mapping m : bom.getMappings()) {
                for (String r : m.getRepositories()) {
                    if (!repositories.containsKey(r)) {
                        throw new InvalidGeneratorMetadataException(
                                m + " of " + bom + "defines an invalid repository id " + r
                                        + ", available repositories " + repositories);
                    }

                }

                for (String b : m.getAdditionalBoms()) {
                    if (!boms.containsKey(b)) {
                        throw new InvalidGeneratorMetadataException(m + " of " + bom
                                + " defines " + "an invalid additional bom id " + b
                                + ", available boms " + boms);
                    }
                }
            }
        }
    }

    /**
     * Update the available Spring Boot versions with the specified capabilities.
     * @param versionsMetadata the Spring Boot boot versions metadata to use
     */
    public void updateSpringBootVersions(List<DefaultMetadataElement> versionsMetadata) {
        this.bootVersions.getContent().clear();
        this.bootVersions.getContent().addAll(versionsMetadata);

        List<Version> bootVersions = this.bootVersions.getContent().stream().map(
                (it) ->Version.parse(it.getId())).collect(Collectors.toList());
        VersionParser parser = new VersionParser(bootVersions);

        this.dependencies.updateVersionRange(parser);
        this.configuration.getEnv().getBoms().values().forEach((it) -> it.updateVersionRange(parser));
    }

    /**
     * Create an URL suitable to download Spring Boot cli for the specified version and
     * extension.
     * @param extension the required extension
     * @return the download URL
     */
    public String createCliDistributionURl(String extension) {
        String bootVersion = defaultId(this.bootVersions);
        return this.configuration.getEnv().getArtifactRepository()
                + "org/springframework/boot/spring-boot-cli/" + bootVersion
                + "/spring-boot-cli-" + bootVersion + "-bin." + extension;
    }

    /**
     * Create a {@link BillOfMaterials} for the spring boot BOM.
     * @param bootVersion the Spring Boot version
     * @param versionProperty the property that contains the version
     * @return a new {@link BillOfMaterials} instance
     */
    public BillOfMaterials createSpringBootBom(String bootVersion, String versionProperty) {
        BillOfMaterials bom = BillOfMaterials.create("org.springframework.boot",
                "spring-boot-dependencies", bootVersion);
        bom.setVersionProperty(new VersionProperty(versionProperty));
        bom.setOrder(100);

        return bom;
    }

    /**
     * Return the defaults for the capabilities defined on this instance.
     * @return the default capabilities
     */
    public Map<String, Object> defaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();

        defaults.put("type", defaultId(this.types));
        defaults.put("bootVersion", defaultId(this.bootVersions));
        defaults.put("packaging", defaultId(this.packagings));
        defaults.put("javaVersion", defaultId(this.javaVersions));
        defaults.put("language", defaultId(this.languages));
        defaults.put("groupId", this.groupId.getContent());
        defaults.put("artifactId", this.artifactId.getContent());
        defaults.put("version", this.version.getContent());
        defaults.put("name", this.name.getContent());
        defaults.put("description", this.description.getContent());
        defaults.put("packageName", this.packageName.getContent());

        return defaults;
    }

    private static String defaultId(Defaultable<? extends DefaultMetadataElement> element) {
        DefaultMetadataElement defaultValue = element.getDefault();
        return defaultValue != null ? defaultValue.getId() : null;
    }

    private static class ArtifactIdCapability extends TextCapability {

        private final TextCapability nameCapability;

        ArtifactIdCapability(TextCapability nameCapability) {
            super("artifactId", "Artifact", "project coordinates (infer archive name)");
            this.nameCapability = nameCapability;
        }

        @Override
        public String getContent() {
            String value = super.getContent();
            return value == null ? this.nameCapability.getContent() : value;
        }
    }

    private static class PackageCapability extends TextCapability {

        private final TextCapability groupId;

        private final TextCapability artifactId;

        PackageCapability(TextCapability groupId, TextCapability artifactId) {
            super("packageName", "Package Name", "root package");
            this.groupId = groupId;
            this.artifactId = artifactId;
        }

        @Override
        public String getContent() {
            String value = super.getContent();

            if (value != null) {
                return value;
            }
            else if (this.groupId.getContent() != null && this.artifactId.getContent() != null) {
                return GeneratorConfiguration.cleanPackageName(
                        this.groupId.getContent() + "." + this.artifactId.getContent());
            }

            return null;
        }
    }
}
