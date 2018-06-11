package com.microsoft.azure.springcloudplayground.dependency;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.microsoft.azure.springcloudplayground.exception.InvalidGeneratorMetadataException;
import com.microsoft.azure.springcloudplayground.metadata.Describable;
import com.microsoft.azure.springcloudplayground.metadata.Link;
import com.microsoft.azure.springcloudplayground.metadata.MetadataElement;
import com.microsoft.azure.springcloudplayground.util.InvalidVersionException;
import com.microsoft.azure.springcloudplayground.util.Version;
import com.microsoft.azure.springcloudplayground.util.VersionParser;
import com.microsoft.azure.springcloudplayground.util.VersionRange;
import org.springframework.util.StringUtils;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Dependency extends MetadataElement implements Describable {

    /**
     * Compile Scope.
     */
    public static final String SCOPE_COMPILE = "compile";

    /**
     * Compile Only Scope.
     */
    public static final String SCOPE_COMPILE_ONLY = "compileOnly";

    /**
     * Runtime Scope.
     */
    public static final String SCOPE_RUNTIME = "runtime";

    /**
     * Provided Scope.
     */
    public static final String SCOPE_PROVIDED = "provided";

    /**
     * Test Scope.
     */
    public static final String SCOPE_TEST = "test";

    /**
     * All scope types.
     */
    public static final List<String> SCOPE_ALL = Collections
            .unmodifiableList(Arrays.asList(SCOPE_COMPILE, SCOPE_RUNTIME,
                    SCOPE_COMPILE_ONLY, SCOPE_PROVIDED, SCOPE_TEST));

    private List<String> aliases = new ArrayList<>();

    private List<String> facets = new ArrayList<>();

    private String groupId;

    private String artifactId;

    private String version;

    private String type;

    private List<Mapping> mappings = new ArrayList<>();

    private String scope = SCOPE_COMPILE;

    private String description;

    private String versionRange;

    @JsonIgnore
    private String versionRequirement;

    @JsonIgnore
    private VersionRange range;

    private String bom;

    private String repository;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int weight;

    /**
     * Specify if the dependency represents a "starter", i.e. the sole presence of that
     * dependency is enough to bootstrap the context.
     */
    private boolean starter = true;

    private List<String> keywords = new ArrayList<>();

    private List<Link> links = new ArrayList<>();

    public Dependency() {
    }

    public Dependency(Dependency dependency) {
        super(dependency);
        this.aliases.addAll(dependency.aliases);
        this.facets.addAll(dependency.facets);
        this.groupId = dependency.groupId;
        this.artifactId = dependency.artifactId;
        this.version = dependency.version;
        this.type = dependency.type;
        this.mappings.addAll(dependency.mappings);
        this.scope = dependency.scope;
        this.description = dependency.description;
        this.versionRange = dependency.versionRange;
        this.versionRequirement = dependency.versionRequirement;
        this.range = dependency.range;
        this.bom = dependency.bom;
        this.repository = dependency.repository;
        this.weight = dependency.weight;
        this.starter = dependency.starter;
        this.keywords.addAll(dependency.keywords);
        this.links.addAll(dependency.links);
    }

    public void setScope(String scope) {
        if (!SCOPE_ALL.contains(scope)) {
            throw new InvalidGeneratorMetadataException(
                    "Invalid scope " + scope + " must be one of " + SCOPE_ALL);
        }
        this.scope = scope;
    }

    public void setVersionRange(String versionRange) {
        this.versionRange = StringUtils.hasText(versionRange) ? versionRange.trim()
                : null;
    }

    /**
     * Returns if the dependency has its coordinates set, i.e. {@code groupId} and
     * {@code artifactId}.
     * @return if the dependency has coordinates
     */
    private boolean hasCoordinates() {
        return this.groupId != null && this.artifactId != null;
    }

    /**
     * Define this dependency as a standard spring boot starter with the specified name.
     * If no name is specified, the root "spring-boot-starter" is assumed.
     * @param name the starter name or {@code null}
     * @return this instance
     */
    public Dependency asSpringBootStarter(String name) {
        this.groupId = "org.springframework.boot";
        this.artifactId = StringUtils.hasText(name) ? "spring-boot-starter-" + name
                : "spring-boot-starter";
        if (StringUtils.hasText(name)) {
            setId(name);
        }
        return this;
    }

    /**
     * Validate the dependency and complete its state based on the available information.
     */
    public void resolve() {
        if (getId() == null) {
            if (!hasCoordinates()) {
                throw new InvalidGeneratorMetadataException(
                        "Invalid dependency, should have at least an id or a groupId/artifactId pair.");
            }
            generateId();
        }
        else if (!hasCoordinates()) {
            // Let"s build the coordinates from the id
            StringTokenizer st = new StringTokenizer(getId(), ":");
            if (st.countTokens() == 1) { // assume spring-boot-starter
                asSpringBootStarter(getId());
            }
            else if (st.countTokens() == 2 || st.countTokens() == 3) {
                this.groupId = st.nextToken();
                this.artifactId = st.nextToken();
                if (st.hasMoreTokens()) {
                    this.version = st.nextToken();
                }
            }
            else {
                throw new InvalidGeneratorMetadataException(
                        "Invalid dependency, id should have the form groupId:artifactId[:version] but got "
                                + getId());
            }
        }
        this.links.forEach(Link::resolve);
        updateVersionRanges(VersionParser.DEFAULT);
    }

    public void updateVersionRanges(VersionParser versionParser) {
        if (this.versionRange != null) {
            try {
                this.range = versionParser.parseRange(this.versionRange);
                this.versionRequirement = this.range.toString();
            }
            catch (InvalidVersionException ex) {
                throw new InvalidGeneratorMetadataException(
                        "Invalid version range '" + this.versionRange + " for "
                                + "dependency with id '" + getId() + "'",
                        ex);
            }
        }
        this.mappings.forEach((it) -> {
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
     * Return a {@link Dependency} instance that has its state resolved against the
     * specified version.
     * @param bootVersion the Spring Boot version
     * @return this instance
     */
    public Dependency resolve(Version bootVersion) {
        for (Mapping mapping : this.mappings) {
            if (mapping.range.match(bootVersion)) {
                Dependency dependency = new Dependency(this);
                dependency.groupId = mapping.groupId != null ? mapping.groupId
                        : this.groupId;
                dependency.artifactId = mapping.artifactId != null ? mapping.artifactId
                        : this.artifactId;
                dependency.version = mapping.version != null ? mapping.version
                        : this.version;
                dependency.versionRequirement = mapping.range.toString();
                dependency.mappings = null;
                return dependency;
            }
        }
        return this;
    }

    /**
     * Specify if this dependency is available for the specified Spring Boot version.
     * @param version the version the check
     * @return of the version matches
     */
    public boolean match(Version version) {
        if (this.range != null) {
            return this.range.match(version);
        }
        return true;
    }

    /**
     * Generate an id using the groupId and artifactId.
     * @return the generated ID
     */
    public String generateId() {
        if (this.groupId == null || this.artifactId == null) {
            throw new IllegalArgumentException("Could not generate id for " + this
                    + ": at least groupId and artifactId must be set.");
        }
        setId(this.groupId + ":" + this.artifactId);
        return getId();
    }

    public List<String> getAliases() {
        return this.aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public List<String> getFacets() {
        return this.facets;
    }

    public void setFacets(List<String> facets) {
        this.facets = facets;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return this.artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Return the default version, can be {@code null} to indicate that the version is
     * managed by the project and does not need to be specified.
     * @return The default version or {@code null}
     */
    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Return the type, can be {@code null} to indicate that the default type should be
     * used (i.e. {@code jar}).
     * @return the type or {@code null}
     */
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Return the dependency mapping if an attribute of the dependency differs according
     * to the Spring Boot version. If no mapping matches, default attributes are used.
     * @return the dependency mappings
     */
    public List<Mapping> getMappings() {
        return this.mappings;
    }

    public void setMappings(List<Mapping> mappings) {
        this.mappings = mappings;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersionRequirement() {
        return this.versionRequirement;
    }

    public void setVersionRequirement(String versionRequirement) {
        this.versionRequirement = versionRequirement;
    }

    public VersionRange getRange() {
        return this.range;
    }

    public void setRange(VersionRange range) {
        this.range = range;
    }

    public String getBom() {
        return this.bom;
    }

    public void setBom(String bom) {
        this.bom = bom;
    }

    public String getRepository() {
        return this.repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public int getWeight() {
        return this.weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isStarter() {
        return this.starter;
    }

    public void setStarter(boolean starter) {
        this.starter = starter;
    }

    public List<String> getKeywords() {
        return this.keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<Link> getLinks() {
        return this.links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public String getScope() {
        return this.scope;
    }

    public String getVersionRange() {
        return this.versionRange;
    }

    @Override
    public String toString() {
        return "Dependency{" + "id='" + getId() + '\'' + ", groupId='" + this.groupId
                + '\'' + ", artifactId='" + this.artifactId + '\'' + ", version='"
                + this.version + '\'' + '}';
    }

    public static Dependency create(String groupId, String artifactId, String version,
                                    String scope) {
        Dependency dependency = withId(null, groupId, artifactId, version);
        dependency.setScope(scope);
        return dependency;
    }

    public static Dependency withId(String id, String groupId, String artifactId,
                                    String version, String scope) {
        Dependency dependency = new Dependency();
        dependency.setId(id);
        dependency.groupId = groupId;
        dependency.artifactId = artifactId;
        dependency.version = version;
        dependency.scope = (scope != null ? scope : SCOPE_COMPILE);
        return dependency;
    }

    public static Dependency withId(String id, String groupId, String artifactId,
                                    String version) {
        return withId(id, groupId, artifactId, version, null);
    }

    public static Dependency withId(String id, String groupId, String artifactId) {
        return withId(id, groupId, artifactId, null);
    }

    public static Dependency withId(String id, String scope) {
        Dependency dependency = withId(id, null, null);
        dependency.setScope(scope);
        return dependency;
    }

    public static Dependency withId(String id) {
        return withId(id, SCOPE_COMPILE);
    }

    /**
     * Map several attribute of the dependency for a given version range.
     */
    public static class Mapping {

        /**
         * The version range of this mapping.
         */
        private String versionRange;

        /**
         * The version to use for this mapping or {@code null} to use the default.
         */
        private String groupId;

        /**
         * The groupId to use for this mapping or {@code null} to use the default.
         */
        private String artifactId;

        /**
         * The artifactId to use for this mapping or {@code null} to use the default.
         */
        private String version;

        @JsonIgnore
        private VersionRange range;

        public String getGroupId() {
            return this.groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return this.artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return this.version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public VersionRange getRange() {
            return this.range;
        }

        public String getVersionRange() {
            return this.versionRange;
        }

        public void setVersionRange(String versionRange) {
            this.versionRange = versionRange;
        }

        public static Mapping create(String range, String groupId, String artifactId,
                                     String version) {
            Mapping mapping = new Mapping();
            mapping.versionRange = range;
            mapping.groupId = groupId;
            mapping.artifactId = artifactId;
            mapping.version = version;
            return mapping;
        }

    }

}
