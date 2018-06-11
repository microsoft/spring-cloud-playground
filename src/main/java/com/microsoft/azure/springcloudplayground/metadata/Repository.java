package com.microsoft.azure.springcloudplayground.metadata;

import java.net.URL;

public class Repository {

    private String name;

    private URL url;

    private boolean snapshotsEnabled;

    public Repository() {
    }

    public Repository(String name, URL url, boolean snapshotsEnabled) {
        this.name = name;
        this.url = url;
        this.snapshotsEnabled = snapshotsEnabled;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getUrl() {
        return this.url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public boolean isSnapshotsEnabled() {
        return this.snapshotsEnabled;
    }

    public void setSnapshotsEnabled(boolean snapshotsEnabled) {
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
            if (other.url != null) {
                return false;
            }
        }
        else if (!this.url.equals(other.url)) {
            return false;
        }
        return true;
    }

}
