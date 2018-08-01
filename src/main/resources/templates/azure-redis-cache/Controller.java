package {{packageName}};

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping("/{name}")
    @Cacheable("azureCache")
    public String getValue(@PathVariable String name) {
        return "Hello " + name;
    }
}
