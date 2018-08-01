package com.microsoft.azure.springcloudplayground.exception;

@SuppressWarnings("serial")
public class InvalidProjectRequestException extends GeneratorException {

    public InvalidProjectRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidProjectRequestException(String message) {
        super(message);
    }

}