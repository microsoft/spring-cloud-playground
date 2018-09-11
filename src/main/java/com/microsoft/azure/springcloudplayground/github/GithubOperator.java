package com.microsoft.azure.springcloudplayground.github;

import com.google.common.io.Files;
import com.microsoft.azure.springcloudplayground.exception.GithubFileException;
import com.microsoft.azure.springcloudplayground.exception.GithubProcessException;
import com.microsoft.azure.springcloudplayground.github.gitdata.*;
import com.microsoft.azure.springcloudplayground.github.metadata.Author;
import lombok.NonNull;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class GithubOperator extends GithubApiWrapper {

    private static final String DEFAULT_EMAIL = "noreply@github.com";

    private final List<GithubEmails> userEmails;

    public GithubOperator(@NonNull String username, @NonNull String token) {
        super(username, token);
        this.userEmails = getGithubEmails();
    }

    private String getContent(@NonNull HttpResponse response) throws GithubProcessException {
        try {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder builder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }

            return builder.toString();
        } catch (IOException e) {
            throw new GithubProcessException("Failed to obtain github api response content", e);
        }
    }

    private <T> T readValue(@NonNull String json, Class<T> clazz) throws GithubProcessException {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new GithubProcessException("Failed to retrieve object from Json", e);
        }
    }

    private List<GithubEmails> getGithubEmails() {
        List<GithubEmails> emails = new ArrayList<>();

        try {
            HttpResponse response = super.getUserEmails();
            emails.addAll(Arrays.asList(readValue(getContent(response), GithubEmails[].class)));
        } catch (GithubProcessException ignore) {
            emails.add(new GithubEmails(DEFAULT_EMAIL, true, true, null));
        }

        return emails;
    }

    private GithubRepository createRepository(@NonNull String name) throws GithubProcessException {
        GithubRepository repository = GithubRepository.builder(name).build();
        HttpResponse response = super.createRepository(repository);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
            throw new GithubProcessException(String.format("Failed to create github repository [%s].", name));
        }

        return repository;
    }

    public void deleteRepository(@NonNull GithubRepository repository) throws GithubProcessException {
        HttpResponse response = super.deleteRepository(repository.getName());

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
            throw new GithubProcessException(String.format("Failed to delete github repository [%s].", repository.getName()));
        }
    }

    private List<GithubCommit> getRepositoryCommits(@NonNull GithubRepository repository) throws GithubProcessException {
        HttpResponse response = super.getAllCommits(repository.getName());

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new GithubProcessException(String.format("Failed to obtain commits from repository [%s].", repository.getName()));
        }

        GithubCommit[] commit = readValue(getContent(response), GithubCommit[].class);

        Assert.isTrue(commit.length == 1, "should contains only one commit");

        return Arrays.asList(commit);
    }

    private GitDataCommit getGitDataCommit(@NonNull GithubRepository repository, @NonNull GithubCommit commit)
            throws GithubProcessException {
        HttpResponse response = super.getGitDataCommit(repository.getName(), commit.getSha());

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new GithubProcessException(String.format("Failed to get commit from repository [%s].", repository.getName()));
        }

        return readValue(getContent(response), GitDataCommit.class);
    }

    private GitDataTree getGitDataTree(@NonNull GithubRepository repository, @NonNull GitDataCommit commit)
            throws GithubProcessException {
        HttpResponse response = super.getGitDataTree(repository.getName(), commit.getTree().getSha());

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new GithubProcessException(String.format("Failed to get tree from repository [%s].", repository.getName()));
        }

        return readValue(getContent(response), GitDataTree.class);
    }

    private GitDataRequestTree getGitDataRequestTree(@NonNull GitDataTree baseTree) {
        GitDataRequestTree requestTree = new GitDataRequestTree();

        requestTree.setBase_tree(baseTree.getSha());
        requestTree.setTree(new ArrayList<>());

        return requestTree;
    }

    private GitDataBlob createGitDataBlob(@NonNull GithubRepository repository, @NonNull GitDataRequestBlob requestBlob)
            throws GithubProcessException {
        HttpResponse response = super.createGitDataBlob(repository.getName(), requestBlob);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
            throw new GithubProcessException(String.format("Failed to create blob from repository [%s].", repository.getName()));
        }

        return readValue(getContent(response), GitDataBlob.class);
    }

    private GitDataRequestBlob getGitDataRequestBlob(@NonNull String filename) {
        try {
            String content = new String(Files.toByteArray(new File(filename)));

            return new GitDataRequestBlob(content, "utf-8");
        } catch (IOException e) {
            throw new GithubFileException(String.format("Failed to read file [%s].", filename), e);
        }
    }

    private GitDataRequestTree.TreeNode getRequestTreeNode(@NonNull String name, @NonNull String sha) {
        return GitDataRequestTree.TreeNode.builder()
                .path(name)
                .mode("100644")
                .type("blob")
                .sha(sha)
                .build();
    }

    private GitDataRequestCommit getGitDateRequestCommit(@NonNull GitDataCommit parent, @NonNull GithubTree tree) {
        Author author = new Author(getUsername(), this.userEmails.get(0).getEmail());
        List<String> parents = Collections.singletonList(parent.getSha());

        return GitDataRequestCommit.builder()
                .message("Add generated project of spring cloud azure")
                .parents(parents)
                .author(author)
                .tree(tree.getSha())
                .build();
    }

    private GitDataCommit createGitDateCommit(@NonNull GithubRepository repository, @NonNull GitDataRequestCommit commit)
            throws GithubProcessException {
        HttpResponse response = super.createGitDataCommit(repository.getName(), commit);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
            throw new GithubProcessException(String.format("Failed to create commit from repository [%s].", repository.getName()));
        }

        return readValue(getContent(response), GitDataCommit.class);
    }

    private GithubTree createGitDataTree(@NonNull GithubRepository repository, @NonNull GitDataRequestTree tree)
            throws GithubProcessException {
        HttpResponse response = super.createGitDataTree(repository.getName(), tree);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
            throw new GithubProcessException(String.format("Failed to create tree from repository [%s].", repository.getName()));
        }

        return readValue(getContent(response), GithubTree.class);
    }

    private GitDataRequestReference getGitDataRequestReference(@NonNull GitDataCommit commit) {
        return new GitDataRequestReference(commit.getSha(), true);
    }

    private List<String> getAllFiles(@NonNull File file) {
        if (file.isDirectory()) {
            List<String> allFiles = new ArrayList<>();

            for (File f : Objects.requireNonNull(file.listFiles())) {
                if (f.isFile()) {
                    allFiles.add(f.getPath());
                } else {
                    allFiles.addAll(getAllFiles(f));
                }
            }

            return allFiles;
        }

        return Collections.singletonList(file.getName());
    }

    private String truncateFileNamePrefix(@NonNull String fileName) {
        Assert.hasText(fileName, "file name should have text.");

        char separator = '=';
        String tmp = fileName;

        do {
            fileName = tmp;
            tmp = fileName.replace(File.separatorChar, separator);
        } while (!tmp.equals(fileName));

        List<String> paths = Arrays.asList(fileName.split(String.valueOf(separator)));

        Assert.isTrue(paths.size() >= 4, "file name should contains at least 4 directory in path");

        return String.join("/", paths.subList(4, paths.size())); // Github use "/" instead of File.separator.
    }

    private GithubTree createGithubTree(@NonNull GithubRepository repository, @NonNull GitDataCommit parentCommit,
                                        @NonNull File dir) throws GithubProcessException {
        List<String> files = getAllFiles(dir);
        GitDataTree tree = getGitDataTree(repository, parentCommit);
        GitDataRequestTree requestTree = getGitDataRequestTree(tree);
        List<GitDataRequestBlob> requestBlobs = files.stream().map(this::getGitDataRequestBlob).collect(Collectors.toList());

        for (int i = 0; i < files.size(); i++) {
            String filename = files.get(i);
            GitDataBlob blob = createGitDataBlob(repository, requestBlobs.get(i));

            requestTree.getTree().add(getRequestTreeNode(truncateFileNamePrefix(filename), blob.getSha()));
        }

        return createGitDataTree(repository, requestTree);
    }

    private void updateGithubRepository(@NonNull GithubRepository repository,
                                        @NonNull GitDataRequestReference reference) throws GithubProcessException {
        HttpResponse response = super.updateGitDataReference(repository.getName(), reference);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new GithubProcessException(String.format("Failed to update reference from repository [%s].", repository.getName()));
        }
    }

    public String createRepository(@NonNull File dir, @NonNull String repositoryName) throws GithubProcessException {
        GithubRepository repository = createRepository(repositoryName);
        GitDataCommit parentCommit = getGitDataCommit(repository, getRepositoryCommits(repository).get(0));
        GithubTree githubTree = createGithubTree(repository, parentCommit, dir);
        GitDataRequestCommit requestCommit = getGitDateRequestCommit(parentCommit, githubTree);
        GitDataCommit commit = createGitDateCommit(repository, requestCommit);
        GitDataRequestReference reference = getGitDataRequestReference(commit);

        updateGithubRepository(repository, reference);

        return repository.getRepositoryUrl(getUsername());
    }
}
