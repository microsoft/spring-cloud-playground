package com.microsoft.azure.springcloudplayground.github.gitdata;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class GitDataRequestTree {

    private String base_tree;

    private List<TreeNode> tree;

    @Data
    @Builder
    public static class TreeNode {

        private String path;

        private String mode;

        private String type;

        private String sha; // sha of one blob
    }
}
