package com.microsoft.azure.springcloudplayground.github.gitdata;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GitDataFileBlob extends GitDataBlob {

    private String filename;

    public static GitDataFileBlob from(@NonNull GitDataBlob blob) {
        GitDataFileBlob fileBlob = new GitDataFileBlob();

        fileBlob.setUrl(blob.getUrl());
        fileBlob.setSha(blob.getSha());

        return fileBlob;
    }

    public GitDataFileBlob with(@NonNull String filename) {
        setFilename(filename);

        return this;
    }
}
