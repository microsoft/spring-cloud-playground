package com.microsoft.azure.springcloudplayground.github.metadata;

import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
public class Committer extends AbstractUser {

    public Committer(@NonNull String name, @NonNull String email) {
        super(name, email);
    }
}
