package com.microsoft.azure.springcloudplayground.github;

import com.microsoft.azure.springcloudplayground.github.gitdata.GitDataTreeNode;
import lombok.Data;

import java.util.List;

@Data
public class GithubTree {

    private String sha;

    private String url;

    private List<GitDataTreeNode> tree;

    private boolean truncated;
}
