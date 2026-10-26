package cl.utem.inf.cpyd.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "credentials")
public class Credential extends PkEntityBase {

    @Column(name = "token", nullable = false, unique = true)
    private UUID token;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "api_key", nullable = false)
    private String apiKey;

    public UUID getToken() {
        return token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
