package org.example.model;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import org.example.api.Role;

public class UserEntity {
    public String username;
    public String password; // is stored hashed by default
    public String phone;
    public String address;
    public Role role;

    public UserEntity() {}

    public UserEntity(String username, String password, String phone, String address, Role role) {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.role = role;
    }

    public Entity toEntity(Datastore datastore) {
        Key key = datastore.newKeyFactory().setKind("User").newKey(username);
        return Entity.newBuilder(key)
                .set("password", password)
                .set("phone", phone)
                .set("address", address)
                .set("role", role.toString())
                .build();
    }

    public static UserEntity fromEntity(Entity entity) {
        return new UserEntity(
                entity.getString("username"),
                entity.getString("password"),
                entity.getString("phone"),
                entity.getString("address"),
                Role.valueOf(entity.getString("role"))
        );
    }
}
