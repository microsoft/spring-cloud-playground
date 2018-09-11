package com.microsoft.azure.springcloudplayground.github.metadata;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@NoArgsConstructor
public abstract class AbstractUser {

    private String name;

    private String email;

    private final String date = getCurrentDate();

    public AbstractUser(@NonNull String name, @NonNull String email) {
        this.name = name;
        this.email = email;
    }

    private String getCurrentDate() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

        return format.format(date);
    }
}
