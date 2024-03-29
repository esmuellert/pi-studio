package site.pistudio.backend.entities.firestore;

import com.google.cloud.datastore.Key;
import com.google.cloud.spring.data.datastore.core.mapping.Entity;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Admin implements Role {
    @Id
    Key key;

    private UUID id;
    private String username;
    private String password;
    private LocalDateTime tokenExpired;
    private String tokenSecret;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public LocalDateTime getTokenExpired() {
        return tokenExpired;
    }

    @Override
    public void setTokenExpired(LocalDateTime token_expired) {
        this.tokenExpired = token_expired;
    }

    @Override
    public String getTokenSecret() {
        return tokenSecret;
    }

    @Override
    public void setTokenSecret(String token_secret) {
        this.tokenSecret = token_secret;
    }
}
