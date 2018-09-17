package {{packageName}};

import org.springframework.boot.SpringApplication;
{{#applicationImports}}
import {{this}};
{{/applicationImports}}

{{#applicationAnnotations}}
{{this}}
{{/applicationAnnotations}}
public class {{applicationName}} {

    public static void main(String[] args) {
        SpringApplication.run({{applicationName}}.class, args);
    }
}
