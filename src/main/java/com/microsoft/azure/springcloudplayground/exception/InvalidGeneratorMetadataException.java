package com.microsoft.azure.springcloudplayground.exception;

public class InvalidGeneratorMetadataException extends GeneratorException {

    public InvalidGeneratorMetadataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidGeneratorMetadataException(String message) {
        super(message);
    }

}
