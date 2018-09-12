package com.microsoft.azure.springcloudplayground.github;

import com.google.common.io.Files;
import com.microsoft.azure.springcloudplayground.exception.GithubFileException;
import com.microsoft.azure.springcloudplayground.exception.GithubProcessException;
import com.microsoft.azure.springcloudplayground.github.gitdata.GitDataBlob;
import com.microsoft.azure.springcloudplayground.github.gitdata.GitDataFileBlob;
import com.microsoft.azure.springcloudplayground.github.gitdata.GitDataRequestBlob;
import lombok.Builder;
import lombok.NonNull;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@Builder
public class GitDataFileBlobCreator implements Callable<GitDataFileBlob> {

    private final GithubRepository repository;

    private final String filename;

    private final String username;

    private final String token;

    private GitDataRequestBlob getGitDataRequestBlob(@NonNull String filename) {
        try {
            String content = new String(Files.toByteArray(new File(filename)));

            return new GitDataRequestBlob(content, "utf-8");
        } catch (IOException e) {
            throw new GithubFileException(String.format("Failed to read file [%s].", filename), e);
        }
    }

    public GitDataFileBlob call() throws GithubProcessException {
        GithubApiWrapper apiWrapper = new GithubApiWrapper(username, token);
        GitDataRequestBlob requestBlob = getGitDataRequestBlob(filename);
        HttpResponse response = apiWrapper.createGitDataBlob(repository.getName(), requestBlob);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
            throw new GithubProcessException(String.format("Failed to create blob from repository [%s].", repository.getName()));
        }

        GitDataBlob blob = apiWrapper.readValue(apiWrapper.getContent(response), GitDataBlob.class);
        return GitDataFileBlob.from(blob).with(filename);
    }
}
