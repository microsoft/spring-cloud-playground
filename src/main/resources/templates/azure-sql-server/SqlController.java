package {{packageName}};

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class SqlController {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @GetMapping("/users")
    public List getUsers() {
        return this.jdbcTemplate.queryForList("SELECT * FROM users").stream().map(Map::values)
                                .collect(Collectors.toList());
    }
}