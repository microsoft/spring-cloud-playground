package com.microsoft.azure.springcloudplayground.github;

import com.microsoft.azure.springcloudplayground.github.metadata.*;
import lombok.Data;

@Data
public class GithubCommit {

    private String sha;

    private String node_id;

    private Commit commit;

    private String url;

    private String html_url;

    private String comments_url;

    private GithubAuthor author;

    private GithubCommitter committer;

    private Parent[] parents;

    @Data
    public static class Commit {

        private Author author;

        private Committer committer;

        private String message;

        private Tree tree;

        private String url;

        private int comment_count;

        private Verification verification;
    }

    @Data
    private static class GithubAuthor {

        private String login;

        private String id;

        private String node_id;

        private String avatar_url;

        private String gravatar_id;

        private String url;

        private String html_url;

        private String followers_url;

        private String following_url;

        private String gists_url;

        private String starred_url;

        private String subscriptions_url;

        private String organizations_url;

        private String repos_url;

        private String events_url;

        private String received_events_url;

        private String type;

        private String site_admin;
    }

    private static class GithubCommitter extends GithubAuthor {

    }
}
