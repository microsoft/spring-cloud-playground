package com.microsoft.azure.springcloudplayground.util;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class VersionProperty implements Serializable, Comparable<VersionProperty> {

    private static final List<Character> SUPPORTED_CHARS = Arrays.asList('.', '-');

    private final String property;

    public VersionProperty(String property) {
        this.property = validateFormat(property);
    }

    /**
     * Return a camel cased representation of this instance.
     * @return the property in camel case format
     */
    public String toCamelCaseFormat() {
        String[] tokens = this.property.split("\\-|\\.");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            String part = tokens[i];
            if (i > 0) {
                part = StringUtils.capitalize(part);
            }
            sb.append(part);
        }
        return sb.toString();
    }

    @JsonValue
    public String toStandardFormat() {
        return this.property;
    }

    private static String validateFormat(String property) {
        for (char c : property.toCharArray()) {
            if (Character.isUpperCase(c)) {
                throw new IllegalArgumentException("Invalid property '" + property
                        + "', must not contain upper case");
            }
            if (!Character.isLetterOrDigit(c) && !SUPPORTED_CHARS.contains(c)) {
                throw new IllegalArgumentException(
                        "Unsupported character '" + c + "' for '" + property + "'");
            }
        }
        return property;
    }

    @Override
    public int compareTo(VersionProperty o) {
        return this.property.compareTo(o.property);
    }

    @Override
    public String toString() {
        return this.property;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VersionProperty that = (VersionProperty) o;

        return this.property.equals(that.property);
    }

    @Override
    public int hashCode() {
        return this.property.hashCode();
    }

}
