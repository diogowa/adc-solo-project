package org.example.api;

public class UsernameRequest {
    public String username;

    public boolean isValid() {
        return username != null && !username.isBlank();
    }
}
