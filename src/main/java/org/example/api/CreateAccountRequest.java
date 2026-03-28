package org.example.api;

import org.example.model.Role;

public class CreateAccountRequest {
    public String username;
    public String password;
    public String confirmation;
    public String phone;
    public String address;
    public Role role; // USER | BOFFICER | ADMIN

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{4,15}");
    }

    private boolean isValidRole(Role role) {
        return role != null;
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
