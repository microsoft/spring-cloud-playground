package com.microsoft.azure.springcloudplayground.processor;

import com.microsoft.azure.springcloudplayground.generator.ProjectRequest;
import com.microsoft.azure.springcloudplayground.generator.ProjectRequestPostProcessor;
import com.microsoft.azure.springcloudplayground.dependency.Dependency;
import com.microsoft.azure.springcloudplayground.util.Version;

public class AbstractProjectRequestPostProcessor implements ProjectRequestPostProcessor {

    /**
     * Determine if the {@link ProjectRequest request} defines the dependency with the
     * specified {@code dependencyId}.
     * @param request the request to handle
     * @param dependencyId the id of a dependency
     * @return {@code true} if the project defines that dependency
     */
    protected boolean hasDependency(ProjectRequest request, String dependencyId) {
        return hasDependencies(request, dependencyId);
    }

    /**
     * Determine if the {@link ProjectRequest request} defines the dependencies with the
     * specified {@code dependenciesId}.
     * @param request the request to handle
     * @param dependenciesId the dependency ids
     * @return {@code true} if the project defines all dependencies
     */
    protected boolean hasDependencies(ProjectRequest request, String... dependenciesId) {
        for (String id : dependenciesId) {
            if (getDependency(request, id) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the {@link Dependency} with the specified {@code id} or {@code null} if the
     * project does not define it.
     * @param request the request to handle
     * @param id the id of a dependency
     * @return the {@link Dependency} with that id or {@code null} if the project does not
     * define such dependency
     */
    protected Dependency getDependency(ProjectRequest request, String id) {
        return request.getResolvedDependencies().stream()
                .filter(d -> id.equals(d.getId())).findFirst().orElse(null);
    }

    /**
     * Specify if the Spring Boot version of the {@link ProjectRequest request} is higher
     * or equal to the specified {@link Version}.
     * @param request the request to handle
     * @param version the minimum version
     * @return {@code true} if the requested version is equal or higher than the specified
     * {@code version}
     */
    protected boolean isSpringBootVersionAtLeastAfter(ProjectRequest request,
                                                      Version version) {
        Version requestVersion = Version.safeParse(request.getBootVersion());
        return version.compareTo(requestVersion) <= 0;
    }

}
