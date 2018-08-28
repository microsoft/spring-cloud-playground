package {{packageName}};

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/keyvault")
public class KeyVaultSecretsController {

    @Value("${your-secret-property-here}")
    private String secretProperty;

    @GetMapping(value = "/property")
    public String getSecretProperty() {
        return this.secretProperty;
    }
}
