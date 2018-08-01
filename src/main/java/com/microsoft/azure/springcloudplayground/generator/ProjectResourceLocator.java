package com.microsoft.azure.springcloudplayground.generator;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

public class ProjectResourceLocator {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Return the binary content of the resource at the specified location.
     * @param location a resource location
     * @return the content of the resource
     */
    @Cacheable("playground.project-resources")
    public byte[] getBinaryResource(String location) {
        try (InputStream stream = getInputStream(location)) {
            return StreamUtils.copyToByteArray(stream);
        }
        catch (IOException ex) {
            throw new IllegalStateException("Cannot get resource", ex);
        }
    }

    /**
     * Return the textual content of the resource at the specified location.
     * @param location a resource location
     * @return the content of the resource
     */
    @Cacheable("playground.project-resources")
    public String getTextResource(String location) {
        try (InputStream stream = getInputStream(location)) {
            return StreamUtils.copyToString(stream, UTF_8);
        }
        catch (IOException ex) {
            throw new IllegalStateException("Cannot get resource", ex);
        }
    }

    private InputStream getInputStream(String location) throws IOException {
        URL url = ResourceUtils.getURL(location);
        return url.openStream();
    }

}
