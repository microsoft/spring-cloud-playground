package com.microsoft.azure.springcloudplayground.github.gitdata;

import lombok.Data;

@Data
public class GitDataTreeNode {

    private String path;

    private String mode;

    private String type;

    private String sha;

    private int size;

    private String url;
}
