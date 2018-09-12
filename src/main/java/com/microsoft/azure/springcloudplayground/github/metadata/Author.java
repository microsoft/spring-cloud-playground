package com.microsoft.azure.springcloudplayground.github.metadata;

import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
public class Author extends AbstractUser {

    public Author(@NonNull String name, @NonNull String email) {
        super(name, email);
    }
}
