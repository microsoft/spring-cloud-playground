package com.microsoft.azure.springcloudplayground.github;

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

    public GithubOperator(@NonNull String username, @NonNull String token) {
        super(username, token);
    }

    private String getContent(@NonNull HttpResponse response) {
        try {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder builder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }

            return builder.toString();
        } catch (IOException e) {
            throw new GithubProcessException("Failed to obtain response content", e);
        }
    }

    private <T> T readValue(@NonNull String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new GithubProcessException("Failed to retrieve object from json", e);
        }
    }

    private GithubRepository createRepository(@NonNull String name) {
        GithubRepository repository = GithubRepository.builder(name).build();
        HttpResponse response = super.createRepository(repository);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
            throw new GithubProcessException("Failed to create project: " + name);
        }

        return repository;
    }

    public void deleteRepository(@NonNull GithubRepository repository) {
        HttpResponse response = super.deleteRepository(repository.getName());

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
            throw new GithubProcessException("Failed to delete: " + repository.getName());
        }
    }

    private List<GithubCommit> getRepositoryCommits(@NonNull GithubRepository repository) {
        HttpResponse response = super.getAllCommits(repository.getName());

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new GithubProcessException("Failed to obtain commits from repo: " + repository.getName());
        }

        GithubCommit[] commit = readValue(getContent(response), GithubCommit[].class);

        Assert.isTrue(commit.length == 1, "should contains only one commit");

        return Arrays.asList(commit);
    }

    private GitDataCommit getGitDataCommit(@NonNull GithubRepository repository, @NonNull GithubCommit commit) {
        HttpResponse response = super.getGitDataCommit(repository.getName(), commit.getSha());

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new GithubProcessException("Failed to obtain commits from repo: " + repository.getName());
        }

        return readValue(getContent(response), GitDataCommit.class);
    }

    private GitDataTree getGitDataTree(@NonNull GithubRepository repository, @NonNull GitDataCommit commit) {
        HttpResponse response = super.getGitDataTree(repository.getName(), commit.getTree().getSha());

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new GithubProcessException("Failed to obtain tree from repo: " + repository.getName());
        }

        return readValue(getContent(response), GitDataTree.class);
    }

    private GitDataRequestTree getGitDataRequestTree(@NonNull GitDataTree baseTree) {
        GitDataRequestTree requestTree = new GitDataRequestTree();

        requestTree.setBase_tree(baseTree.getSha());
        requestTree.setTree(new ArrayList<>());

        return requestTree;
    }

    private GitDataBlob createGitDataBlob(@NonNull GithubRepository repository, @NonNull GitDataRequestBlob requestBlob) {
        HttpResponse response = super.createGitDataBlob(repository.getName(), requestBlob);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
            throw new GithubProcessException("Failed to create tree from repo: " + repository.getName());
        }

        return readValue(getContent(response), GitDataBlob.class);
    }

    private GitDataRequestBlob getGitDataRequestBlob(@NonNull String filename) {
        String content = "";

        return new GitDataRequestBlob(content, "utf-8");
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
        Author author = new Author(getUsername(), "fake-email");
        List<String> parents = Collections.singletonList(parent.getSha());

        return GitDataRequestCommit.builder()
                .message("Add generated project of spring cloud azure")
                .parents(parents)
                .author(author)
                .tree(tree.getSha())
                .build();
    }

    private GitDataCommit createGitDateCommit(@NonNull GithubRepository repository, @NonNull GitDataRequestCommit commit) {
        HttpResponse response = super.createGitDataCommit(repository.getName(), commit);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
            throw new GithubProcessException("Failed to create commit from repo: " + repository.getName());
        }

        return readValue(getContent(response), GitDataCommit.class);
    }

    private GithubTree createGitDataTree(@NonNull GithubRepository repository, @NonNull GitDataRequestTree tree) {
        HttpResponse response = super.createGitDataTree(repository.getName(), tree);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
            throw new GithubProcessException("Failed to create tree from repo: " + repository.getName());
        }

        return readValue(getContent(response), GithubTree.class);
    }

    private GitDataRequestReference getGitDataRequestReference(@NonNull GitDataCommit commit) {
        return new GitDataRequestReference(commit.getSha(), true);
    }

    private void updateGithubRepository(@NonNull GithubRepository repository,
                                        @NonNull GitDataRequestReference reference) {
        HttpResponse response = super.updateGitDataReference(repository.getName(), reference);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new GithubProcessException("Failed to update reference from repo: " + repository.getName());
        }
    }

    public List<String> getAllFileNames(@NonNull File file) {
        if (file.isDirectory()) {
            List<String> fileNames = new ArrayList<>();

            for (File f : Objects.requireNonNull(file.listFiles())) {
                if (f.isFile()) {
                    fileNames.add(f.getName());
                } else {
                    fileNames.addAll(getAllFileNames(f));
                }
            }

            return fileNames;
        }

        return Collections.singletonList(file.getName());
    }

    public void createRepository(@NonNull File dir) {
        GithubRepository repository = createRepository("spring-cloud-azure-demo");
        List<GithubCommit> commits = getRepositoryCommits(repository);
        GitDataCommit parentCommit = getGitDataCommit(repository, commits.get(0));
        GitDataTree tree = getGitDataTree(repository, parentCommit);
        GitDataRequestTree requestTree = getGitDataRequestTree(tree);

        List<String> fileNames = getAllFileNames(dir);
        List<GitDataBlob> blobs = fileNames.stream().map(this::getGitDataRequestBlob)
                .map(b -> createGitDataBlob(repository, b)).collect(Collectors.toList());

        for (int i = 0; i < fileNames.size(); i++) {
            String filename = fileNames.get(i);
            String sha = blobs.get(i).getSha();

            requestTree.getTree().add(getRequestTreeNode(filename, sha));
        }

        GithubTree githubTree = createGitDataTree(repository, requestTree);
        GitDataRequestCommit requestCommit = getGitDateRequestCommit(parentCommit, githubTree);
        GitDataCommit commit = createGitDateCommit(repository, requestCommit);
        GitDataRequestReference reference = getGitDataRequestReference(commit);

        updateGithubRepository(repository, reference);

        deleteRepository(repository);
    }
}
