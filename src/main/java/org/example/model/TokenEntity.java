package org.example.model;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import org.example.api.Role;

import java.util.UUID;

public class TokenEntity {
    private static final long TOKEN_VALIDITY = 15 * 60 * 1000; // 15 minutes

    public String tokenId;
    public String username;
    public Role role;
    public long issuedAt;
    public long expiresAt;

    public TokenEntity() {}

    public TokenEntity(String username, Role role) {
        this.tokenId = UUID.randomUUID().toString();
        this.username = username;
        this.role = role;
        this.issuedAt = System.currentTimeMillis();
        this.expiresAt = issuedAt + TOKEN_VALIDITY;
    }

    private TokenEntity(String tokenId, String username, Role role, long issuedAt, long expiresAt) {
        this.tokenId = tokenId;
        this.username = username;
        this.role = role;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public Entity toEntity(Datastore datastore) {
        Key key = datastore.newKeyFactory().setKind("UserToken").newKey(tokenId);
        return Entity.newBuilder(key)
                .set("username", username)
                .set("role", role.toString())
                .set("issuedAt", issuedAt)
                .set("expiresAt", expiresAt)
                .build();
    }

    public static TokenEntity fromEntity(Entity entity) {
        return new TokenEntity(
                entity.getString("tokenId"),
                entity.getString("username"),
                Role.valueOf(entity.getString("role")),
                entity.getLong("issuedAt"),
                entity.getLong("expiresAt")
        );
    }

    public boolean isTokenValid() {
        return System.currentTimeMillis() <= expiresAt;
    }
}
