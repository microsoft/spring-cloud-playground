package com.microsoft.azure.springcloudplayground.util;

import org.springframework.util.Assert;

public class VersionRange {

    private final Version lowerVersion;

    private final boolean lowerInclusive;

    private final Version higherVersion;

    private final boolean higherInclusive;

    // For Jackson
    @SuppressWarnings("unused")
    private VersionRange() {
        this(null, false, null, false);
    }

    protected VersionRange(Version lowerVersion, boolean lowerInclusive,
                           Version higherVersion, boolean higherInclusive) {
        this.lowerVersion = lowerVersion;
        this.lowerInclusive = lowerInclusive;
        this.higherVersion = higherVersion;
        this.higherInclusive = higherInclusive;
    }

    public VersionRange(Version startingVersion) {
        this(startingVersion, true, null, false);
    }

    /**
     * Specify if the {@link Version} matches this range. Returns {@code true} if the
     * version is contained within this range, {@code false} otherwise.
     * @param version the version to check
     * @return {@code true} if the version matches
     */
    public boolean match(Version version) {
        Assert.notNull(version, "Version must not be null");
        int lower = this.lowerVersion.compareTo(version);
        if (lower > 0) {
            return false;
        }
        else if (!this.lowerInclusive && lower == 0) {
            return false;
        }
        if (this.higherVersion != null) {
            int higher = this.higherVersion.compareTo(version);
            if (higher < 0) {
                return false;
            }
            else if (!this.higherInclusive && higher == 0) {
                return false;
            }
        }
        return true;
    }

    public Version getLowerVersion() {
        return this.lowerVersion;
    }

    public boolean isLowerInclusive() {
        return this.lowerInclusive;
    }

    public Version getHigherVersion() {
        return this.higherVersion;
    }

    public boolean isHigherInclusive() {
        return this.higherInclusive;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.lowerVersion != null) {
            sb.append(this.lowerInclusive ? ">=" : ">").append(this.lowerVersion);
        }
        if (this.higherVersion != null) {
            sb.append(" and ").append(this.higherInclusive ? "<=" : "<")
                    .append(this.higherVersion);
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.higherInclusive ? 1231 : 1237);
        result = prime * result
                + ((this.higherVersion == null) ? 0 : this.higherVersion.hashCode());
        result = prime * result + (this.lowerInclusive ? 1231 : 1237);
        result = prime * result
                + ((this.lowerVersion == null) ? 0 : this.lowerVersion.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VersionRange other = (VersionRange) obj;
        if (this.higherInclusive != other.higherInclusive) {
            return false;
        }
        if (this.higherVersion == null) {
            if (other.higherVersion != null) {
                return false;
            }
        }
        else if (!this.higherVersion.equals(other.higherVersion)) {
            return false;
        }
        if (this.lowerInclusive != other.lowerInclusive) {
            return false;
        }
        if (this.lowerVersion == null) {
            if (other.lowerVersion != null) {
                return false;
            }
        }
        else if (!this.lowerVersion.equals(other.lowerVersion)) {
            return false;
        }
        return true;
    }

}
