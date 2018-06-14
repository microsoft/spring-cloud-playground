package com.microsoft.azure.springcloudplayground.metadata;

import com.microsoft.azure.springcloudplayground.exception.InvalidGeneratorMetadataException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class GeneratorConfiguration {

    /**
     * Environment options.
     */
    @NestedConfigurationProperty
    private final Env env = new Env();

    public Env getEnv() {
        return this.env;
    }

    public void validate() {
        this.env.validate();
    }

    public void merge(GeneratorConfiguration other) {
        this.env.merge(other.env);
    }

    /**
     * Generate a suitable application name based on the specified name. If no suitable
     * application name can be generated from the specified {@code name}, the
     * {@link Env#getFallbackApplicationName()} is used instead.
     * <p>
     * No suitable application name can be generated if the name is {@code null} or if it
     * contains an invalid character for a class identifier.
     * @param name The the source name
     * @return the generated application name
     * @see Env#getFallbackApplicationName()
     * @see Env#getInvalidApplicationNames()
     */
    public String generateApplicationName(String name) {
        if (!StringUtils.hasText(name)) {
            return this.env.fallbackApplicationName;
        }

        String text = splitCamelCase(name.trim());
        // TODO: fix this
        String result = unsplitWords(text);

        if (!result.endsWith("Application")) {
            result = result + "Application";
        }

        String candidate = StringUtils.capitalize(result);

        if (hasInvalidChar(candidate) || this.env.invalidApplicationNames.contains(candidate)) {
            return this.env.fallbackApplicationName;
        }
        else {
            return candidate;
        }
    }

    /**
     * Clean the specified package name if necessary. If the package name cannot be
     * transformed to a valid package name, the {@code defaultPackageName} is used
     * instead.
     * <p>
     * The package name cannot be cleaned if the specified {@code packageName} is
     * {@code null} or if it contains an invalid character for a class identifier.
     * @param packageName The package name
     * @param defaultPackageName the default package name
     * @return the cleaned package name
     * @see Env#getInvalidPackageNames()
     */
    public String cleanPackageName(String packageName, String defaultPackageName) {
        if (!StringUtils.hasText(packageName)) {
            return defaultPackageName;
        }

        String candidate = cleanPackageName(packageName);

        if (hasInvalidChar(candidate.replace(".", "")) || this.env.invalidPackageNames.contains(candidate)) {
            return defaultPackageName;
        }
        else {
            return candidate;
        }
    }

    static String cleanPackageName(String packageName) {
        String[] elements = packageName.trim().replaceAll("-", "").split("\\W+");
        StringBuilder sb = new StringBuilder();

        for (String element : elements) {
            element = element.replaceFirst("^[0-9]+(?!$)", "");
            if (!element.matches("[0-9]+") && sb.length() > 0) {
                sb.append(".");
            }
            sb.append(element);
        }

        return sb.toString();
    }

    private static String unsplitWords(String text) {
        return String.join("", Arrays.stream(text.split("(_|-| |:)+"))
                .map(StringUtils::capitalize).toArray(String[]::new));
    }

    private static String splitCamelCase(String text) {
        return String.join("", Arrays.stream(text.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"))
                .map((it) -> StringUtils.capitalize(it.toLowerCase()))
                .toArray(String[]::new));
    }

    private static boolean hasInvalidChar(String text) {
        if (!Character.isJavaIdentifierStart(text.charAt(0))) {
            return true;
        }

        if (text.length() > 1) {
            for (int i = 1; i < text.length(); i++) {
                if (!Character.isJavaIdentifierPart(text.charAt(i))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Defines additional environment settings.
     */
    public static class Env {

        /**
         * The url of the repository servicing distribution bundle.
         */
        @Getter
        private String artifactRepository = "https://repo.spring.io/release/";

        /**
         * The metadata url of the Spring Boot project.
         */
        @Getter
        @Setter
        private String springBootMetadataUrl = "https://spring.io/project_metadata/spring-boot";

        /**
         * Tracking code for Google Analytics. Only enabled if a value is explicitly
         * provided.
         */
        @Getter
        @Setter
        private String googleAnalyticsTrackingCode;

        /**
         * The application name to use if none could be generated.
         */
        @Getter
        @Setter
        private String fallbackApplicationName = "Application";

        /**
         * The list of invalid application names. If such name is chosen or generated, the
         * "fallbackApplicationName" should be used instead.
         */
        @Getter
        @Setter
        private List<String> invalidApplicationNames = new ArrayList<>(
                Arrays.asList("SpringApplication", "SpringBootApplication"));

        /**
         * The list of invalid package names. If such name is chosen or generated, the the
         * default package name should be used instead.
         */
        @Getter
        @Setter
        private List<String> invalidPackageNames = new ArrayList<>(
                Collections.singletonList("org.springframework"));

        /**
         * Force SSL support. When enabled, any access using http generate https links.
         */
        @Getter
        @Setter
        private boolean forceSsl = true;

        /**
         * The "BillOfMaterials" that are referenced in this instance, identified by an
         * arbitrary identifier that can be used in the dependencies definition.
         */
        @Getter
        private final Map<String, BillOfMaterials> boms = new LinkedHashMap<>();

        /**
         * The "Repository" instances that are referenced in this instance, identified by
         * an arbitrary identifier that can be used in the dependencies definition.
         */
        @Getter
        private final Map<String, Repository> repositories = new LinkedHashMap<>();

        /**
         * Gradle-specific settings.
         */
        @Getter
        @NestedConfigurationProperty
        private final Gradle gradle = new Gradle();

        /**
         * Maven-specific settings.
         */
        @Getter
        @NestedConfigurationProperty
        private final Maven maven = new Maven();

        public Env() {
            try {
                this.repositories.put("spring-snapshots",
                        new Repository("Spring Snapshots", new URL("https://repo.spring.io/snapshot"), true));
                this.repositories.put("spring-milestones",
                        new Repository("Spring Milestones", new URL("https://repo.spring.io/milestone"), false));
            }
            catch (MalformedURLException e) {
                throw new IllegalStateException("Cannot parse URL", e);
            }
        }

        public void setArtifactRepository(String artifactRepository) {
            if (!artifactRepository.endsWith("/")) {
                artifactRepository = artifactRepository + "/";
            }

            this.artifactRepository = artifactRepository;
        }

        public void validate() {
            this.maven.parent.validate();
            this.boms.forEach((k, v) -> v.validate());
        }

        public void merge(Env other) {
            this.artifactRepository = other.artifactRepository;
            this.springBootMetadataUrl = other.springBootMetadataUrl;
            this.googleAnalyticsTrackingCode = other.googleAnalyticsTrackingCode;
            this.fallbackApplicationName = other.fallbackApplicationName;
            this.invalidApplicationNames = other.invalidApplicationNames;
            this.forceSsl = other.forceSsl;
            this.gradle.merge(other.gradle);
            this.maven.merge(other.maven);
            other.boms.forEach(this.boms::putIfAbsent);
            other.repositories.forEach(this.repositories::putIfAbsent);
        }

        /**
         * Gradle details.
         */
        public static class Gradle {

            /**
             * Version of the "dependency-management-plugin" to use.
             */
            private String dependencyManagementPluginVersion = "1.0.0.RELEASE";

            private void merge(Gradle other) {
                this.dependencyManagementPluginVersion = other.dependencyManagementPluginVersion;
            }

            public String getDependencyManagementPluginVersion() {
                return this.dependencyManagementPluginVersion;
            }

            public void setDependencyManagementPluginVersion(String dependencyManagementPluginVersion) {
                this.dependencyManagementPluginVersion = dependencyManagementPluginVersion;
            }
        }

        /**
         * Maven details.
         */
        public static class Maven {

            /**
             * Custom parent pom to use for generated projects.
             */
            @Getter
            private final ParentPom parent = new ParentPom();

            private void merge(Maven other) {
                this.parent.groupId = other.parent.groupId;
                this.parent.artifactId = other.parent.artifactId;
                this.parent.version = other.parent.version;
                this.parent.includeSpringBootBom = other.parent.includeSpringBootBom;
            }

            /**
             * Resolve the parent pom to use. If no custom parent pom is set, the standard
             * spring boot parent pom with the specified {@code bootVersion} is used.
             * @param bootVersion The Spring Boot version
             * @return the parent POM
             */
            public ParentPom resolveParentPom(String bootVersion) {
                ParentPom pom = new ParentPom("org.springframework.boot", "spring-boot-starter-parent", bootVersion);
                return StringUtils.hasText(this.parent.groupId) ? this.parent : pom;
            }

            /**
             * Parent POM details.
             */
            @NoArgsConstructor
            public static class ParentPom {

                /**
                 * Parent pom groupId.
                 */
//                @Getter
//                @Setter
                private String groupId;

                public String getGroupId() {
                    return this.groupId;
                }

                public void setGroupId(String groupId) {
                    this.groupId = groupId;
                }

                /**
                 * Parent pom artifactId.
                 */
                @Getter
                @Setter
                private String artifactId;

                /**
                 * Parent pom version.
                 */
                @Getter
                @Setter
                private String version;

                /**
                 * Add the "spring-boot-dependencies" BOM to the project.
                 */
                @Getter
                @Setter
                private boolean includeSpringBootBom;

                public ParentPom(String groupId, String artifactId, String version) {
                    this.groupId = groupId;
                    this.artifactId = artifactId;
                    this.version = version;
                }

                public void validate() {
                    if (!StringUtils.hasText(this.groupId) && !StringUtils.hasText(this.artifactId)
                            && !StringUtils.hasText(this.version)) {
                        return;
                    }

                    if (StringUtils.hasText(this.groupId) && StringUtils.hasText(this.artifactId)
                            && StringUtils.hasText(this.version)) {
                        return;
                    }

                    throw new InvalidGeneratorMetadataException("Custom maven pom "
                            + "requires groupId, artifactId and version");
                }
            }
        }
    }
}
