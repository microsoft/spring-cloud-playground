package com.microsoft.azure.springcloudplayground.github.gitdata;

import lombok.Data;

import java.util.List;

@Data
public class GitDataTree {

    private String sha;

    private String url;

    private List<GitDataTreeNode> tree;

    private boolean truncated;
}
