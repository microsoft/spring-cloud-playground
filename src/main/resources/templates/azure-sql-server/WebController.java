package {{packageName}};

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/sql")
public class WebController {

    @Autowired
    private UserRepository repository;

    @GetMapping("/users")
    public List<User> getUsers() {
        List<User> foundUser = new ArrayList<>();

        this.repository.findAll().forEach(foundUser::add);

        return foundUser;
    }

    @PostMapping("/user")
    public User postUser(@RequestBody User user) {
        this.repository.save(user);

        return user;
    }
}
