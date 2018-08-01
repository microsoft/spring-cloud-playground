package com.microsoft.azure.springcloudplayground.metadata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URL;

@NoArgsConstructor
public class Repository {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private URL url;

    @Getter
    @Setter
    private boolean snapshotsEnabled;

    public Repository(String name, URL url, boolean snapshotsEnabled) {
        this.name = name;
        this.url = url;
        this.snapshotsEnabled = snapshotsEnabled;
    }

    @Override
    public String toString() {
        return "Repository [" + (this.name != null ? "name=" + this.name + ", " : "")
                + (this.url != null ? "url=" + this.url + ", " : "") + "snapshotsEnabled="
                + this.snapshotsEnabled + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + (this.snapshotsEnabled ? 1231 : 1237);
        result = prime * result + ((this.url == null) ? 0 : this.url.hashCode());
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

        Repository other = (Repository) obj;

        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!this.name.equals(other.name)) {
            return false;
        }

        if (this.snapshotsEnabled != other.snapshotsEnabled) {
            return false;
        }

        if (this.url == null) {
            return other.url == null;
        }
        else {
            return this.url.equals(other.url);
        }
    }
}
