package com.microsoft.azure.springcloudplayground.github.gitdata;

import com.microsoft.azure.springcloudplayground.github.metadata.Author;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GitDataRequestCommit {

    private String message;

    private Author author;

    private List<String> parents; // parent commit sha

    private String tree; // tree sha
}
