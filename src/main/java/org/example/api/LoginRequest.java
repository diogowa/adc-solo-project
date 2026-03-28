package org.example.api;

public class LoginRequest extends Request {
    public String username;
    public String password;

    public boolean isValid() {
        return notEmptyOrBlank(username)
                && notEmptyOrBlank(password);
    }
}
