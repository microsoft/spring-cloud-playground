package com.microsoft.azure.springcloudplayground.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class PropertyLoader {

    private String filename;

    private Properties properties;

    public PropertyLoader(@NonNull String filename) {
        this.filename = filename;
    }

    private void initializeProperties() {
        InputStream inStream = null;

        try {
            inStream = PropertyLoader.class.getResourceAsStream(this.filename);

            if (inStream != null) {
                this.properties = new Properties();
                this.properties.load(inStream);
            }
        } catch (IOException e) {
            log.warn(String.format("Failed to open file %s, will omit IOException.", this.filename));
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    log.warn(String.format("Unable to close file %s, will omit IOException.", this.filename));
                }
            }
        }
    }

    public String getPropertyValue(@NonNull String propertyName) {
        String value = "Unknown-Property";

        if (this.properties == null) {
            this.initializeProperties();
        }

        // initializeProperties may failure and leave this.properties still null.
        if (this.properties != null) {
            value = this.properties.getProperty(propertyName);
        }

        return value;
    }
}
