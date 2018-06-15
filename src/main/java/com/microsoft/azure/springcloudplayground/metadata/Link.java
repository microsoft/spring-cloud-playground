package com.microsoft.azure.springcloudplayground.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.microsoft.azure.springcloudplayground.exception.InvalidGeneratorMetadataException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor
public class Link {

    private static final Pattern VARIABLE_REGEX = Pattern.compile("\\{(\\w+)\\}");

    /**
     * The relation of the link.
     */
    @Getter
    @Setter
    private String rel;

    /**
     * The URI the link is pointing to.
     */
    @Getter
    @Setter
    private String href;

    /**
     * Specify if the URI is templated.
     */
    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean templated;

    @JsonIgnore
    private final Set<String> templateVariables = new LinkedHashSet<>();

    /**
     * A description of the link.
     */
    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    private Link(String rel, String href) {
        this(rel, href, null);
    }

    private Link(String rel, String href, String description) {
        this.rel = rel;
        this.href = href;
        this.description = description;
    }

    private Link(String rel, String href, boolean templated) {
        this(rel, href);
        this.templated = templated;
    }

    public Set<String> getTemplateVariables() {
        return Collections.unmodifiableSet(this.templateVariables);
    }

    public void resolve() {
        if (this.rel == null) {
            throw new InvalidGeneratorMetadataException("Invalid link " + this + ": rel attribute is mandatory");
        }

        if (this.href == null) {
            throw new InvalidGeneratorMetadataException("Invalid link " + this + ": href attribute is mandatory");
        }

        Matcher matcher = VARIABLE_REGEX.matcher(this.href);

        while (matcher.find()) {
            String variable = matcher.group(1);
            this.templateVariables.add(variable);
        }

        this.templated = !this.templateVariables.isEmpty();
    }

    /**
     * Expand the link using the specified parameters.
     * @param parameters the parameters value
     * @return an URI where all variables have been expanded
     */
    public URI expand(Map<String, String> parameters) {
        AtomicReference<String> result = new AtomicReference<>(this.href);

        this.templateVariables.forEach((var) -> {
            Object value = parameters.get(var);
            if (value == null) {
                throw new IllegalArgumentException("Could not expand " + this.href
                        + ", missing value for '" + var + "'");
            }
            result.set(result.get().replace("{" + var + "}", value.toString()));
        });

        try {
            return new URI(result.get());
        }
        catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid URL", e);
        }
    }

    public static Link create(String rel, String href) {
        return new Link(rel, href);
    }

    public static Link create(String rel, String href, String description) {
        return new Link(rel, href, description);
    }

    public static Link create(String rel, String href, boolean templated) {
        return new Link(rel, href, templated);
    }
}
