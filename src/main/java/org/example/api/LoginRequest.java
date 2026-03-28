package org.example.api;

public class LoginRequest {
    public String username;
    public String password;

    public boolean isValid() {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank();
    }
}
