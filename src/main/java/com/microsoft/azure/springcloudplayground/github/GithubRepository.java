package com.microsoft.azure.springcloudplayground.github;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder(builderMethodName = "hiddenBuilder")
@Data
public class GithubRepository {

    private String name;

    private String homepage;

    private boolean auto_init;

    public static GithubRepositoryBuilder builder(@NonNull String name) {
        return hiddenBuilder()
                .name(name)
                .homepage("https://github.com")
                .auto_init(true);
    }

    public String getRepositoryUrl(@NonNull String username) {
        String url = "https://github.com/%s/%s";

        return String.format(url, username, name);
    }
}
