package com.microsoft.azure.springcloudplayground.exception;

public class GithubFileException extends RuntimeException {

    public GithubFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public GithubFileException(String message) {
        super(message);
    }
}
