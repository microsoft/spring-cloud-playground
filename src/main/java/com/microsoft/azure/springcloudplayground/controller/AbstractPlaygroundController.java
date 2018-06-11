package com.microsoft.azure.springcloudplayground.controller;

import com.microsoft.azure.springcloudplayground.exception.InvalidProjectRequestException;
import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadata;
import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadataProvider;
import com.microsoft.azure.springcloudplayground.metadata.TypeCapability;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractPlaygroundController {

    protected final GeneratorMetadataProvider metadataProvider;

    private final Function<String, String> linkTo;

    private Boolean forceSsl;

    protected AbstractPlaygroundController(GeneratorMetadataProvider metadataProvider,
                                           ResourceUrlProvider resourceUrlProvider) {
        this.metadataProvider = metadataProvider;
        this.linkTo = (link) -> {
            String result = resourceUrlProvider.getForLookupPath(link);
            return result == null ? link : result;
        };
    }

    public boolean isForceSsl() {
        if (this.forceSsl == null) {
            this.forceSsl = this.metadataProvider.get().getConfiguration().getEnv()
                    .isForceSsl();
        }
        return this.forceSsl;

    }

    @ExceptionHandler
    public void invalidProjectRequest(HttpServletResponse response,
                                      InvalidProjectRequestException ex) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    /**
     * Render the home page with the specified template.
     * @param model the model data
     */
    protected void renderHome(Map<String, Object> model) {
        GeneratorMetadata metadata = this.metadataProvider.get();

        model.put("serviceUrl", generateAppUrl());
        BeanWrapperImpl wrapper = new BeanWrapperImpl(metadata);
        for (PropertyDescriptor descriptor : wrapper.getPropertyDescriptors()) {
            if ("types".equals(descriptor.getName())) {
                model.put("types", removeTypes(metadata.getTypes()));
            }
            else {
                model.put(descriptor.getName(),
                        wrapper.getPropertyValue(descriptor.getName()));
            }
        }

        // Google analytics support
        model.put("trackingCode",
                metadata.getConfiguration().getEnv().getGoogleAnalyticsTrackingCode());

    }

    public Function<String, String> getLinkTo() {
        return this.linkTo;
    }

    private TypeCapability removeTypes(TypeCapability types) {
        TypeCapability result = new TypeCapability();
        result.setDescription(types.getDescription());
        result.setTitle(types.getTitle());
        result.getContent().addAll(types.getContent());
        // Only keep project type
        result.getContent().removeIf((t) -> !"project".equals(t.getTags().get("format")));
        return result;
    }

    /**
     * Generate a full URL of the service, mostly for use in templates.
     * @return the app URL
     * @see com.microsoft.azure.springcloudplayground.metadata.GeneratorConfiguration.Env#isForceSsl()
     */
    protected String generateAppUrl() {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder
                .fromCurrentServletMapping();
        if (isForceSsl()) {
            builder.scheme("https");
        }
        return builder.build().toString();
    }

}
