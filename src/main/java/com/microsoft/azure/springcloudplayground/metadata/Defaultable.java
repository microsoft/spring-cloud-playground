package com.microsoft.azure.springcloudplayground.metadata;

public interface Defaultable<T> {

    /**
     * Return the default value.
     * @return the default value
     */
    T getDefault();
}

