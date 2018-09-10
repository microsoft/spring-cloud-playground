package com.microsoft.azure.springcloudplayground.exception;


public class GithubProcessException extends RuntimeException {

    public GithubProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public GithubProcessException(String message) {
        super(message);
    }
}
