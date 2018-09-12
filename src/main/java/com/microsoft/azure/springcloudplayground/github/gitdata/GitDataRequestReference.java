package com.microsoft.azure.springcloudplayground.github.gitdata;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GitDataRequestReference {

    private String sha; // the sha of commit

    private boolean force;
}
