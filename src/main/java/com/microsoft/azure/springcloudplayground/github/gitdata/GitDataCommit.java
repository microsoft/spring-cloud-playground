package com.microsoft.azure.springcloudplayground.github.gitdata;

import com.microsoft.azure.springcloudplayground.github.metadata.*;
import lombok.Data;

@Data
public class GitDataCommit {

    private String sha;

    private String node_id;

    private String url;

    private String html_url;

    private Author author;

    private Committer committer;

    private Tree tree;

    private String message;

    private Parent[] parents;

    private Verification verification;
}
