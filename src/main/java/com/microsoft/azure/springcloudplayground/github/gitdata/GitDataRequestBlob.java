package com.microsoft.azure.springcloudplayground.github.gitdata;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GitDataRequestBlob {

    private String content;

    private String encoding;
}
