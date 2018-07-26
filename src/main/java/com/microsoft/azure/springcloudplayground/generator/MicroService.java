package com.microsoft.azure.springcloudplayground.generator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MicroService {

    private String name;

    private List<String> modules;

    private int port;
}
