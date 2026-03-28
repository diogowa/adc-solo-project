package org.example.api;

public class ChangeUserPasswordRequest {
    public String username;
    public String oldPassword;
    public String newPassword;

    public boolean isValid() {
        return username != null && !username.isBlank()
                && oldPassword != null && !oldPassword.isBlank()
                && newPassword != null && !newPassword.isBlank();
    }
}
