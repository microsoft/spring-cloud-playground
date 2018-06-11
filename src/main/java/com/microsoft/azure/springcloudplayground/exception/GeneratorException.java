package com.microsoft.azure.springcloudplayground.exception;

@SuppressWarnings("serial")
public class GeneratorException extends RuntimeException {

    public GeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeneratorException(String message) {
        super(message);
    }

}

