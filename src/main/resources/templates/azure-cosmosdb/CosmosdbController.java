package {{packageName}};

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cosmosdb")
public class CosmosdbController {

    private static final User USER = new User("id", "first-name", "last-name", "address");

    @Autowired
    private UserRepository repository;

    @GetMapping(value = "/users")
    public List<User> getUsers() {
        this.repository.save(USER);
        return Lists.newArrayList(this.repository.findAll());
    }

    @PostMapping(value = "/user")
    public User putUser(@RequestBody User user) {
        return this.repository.save(user);
    }
}
