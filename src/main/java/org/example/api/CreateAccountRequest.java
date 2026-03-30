package org.example.api;

import org.example.model.Role;

public class CreateAccountRequest {
    public String username;
    public String password;
    public String confirmation;
    public String phone;
    public String address;
    public String role;

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{4,15}");
    }

    public boolean isValidRole(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        try {
            Role.valueOf(role);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isValid() {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank()
                && confirmation != null && !confirmation.isBlank()
                && address != null && !address.isBlank()
                && isValidPhone(phone)
                && isValidRole(role)
                && username.contains("@")
                && password.equals(confirmation);
    }
}
