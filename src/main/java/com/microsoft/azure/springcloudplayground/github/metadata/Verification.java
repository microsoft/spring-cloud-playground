package com.microsoft.azure.springcloudplayground.github.metadata;

import lombok.Data;

@Data
public class Verification {

    private boolean isVerified;

    private String reason;

    private String signature;

    private String payload;
}
