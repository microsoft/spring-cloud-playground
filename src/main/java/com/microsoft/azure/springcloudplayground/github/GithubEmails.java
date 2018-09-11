package com.microsoft.azure.springcloudplayground.github;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubEmails {

    private String email;

    private boolean primary;

    private boolean verified;

    private String visibility;
}
