package org.example.util;

import java.util.logging.Logger;

enum ROLE {
    USER,
    BOFFICER,
    ADMIN
}

public class RegisterData {
    private static final Logger LOG = Logger.getLogger(RegisterData.class.getName());

    public String username;
    public String password;
    public String confirmation;
    public String email;
    public String phone;
    public String address;
    public String role; // USER | BOFFICER | ADMIN

    public RegisterData() {}

    public RegisterData(String username, String password, String confirmation, String email, String phone, String address, String role) {
        this.username = username;
        this.password = password;
        this.confirmation = confirmation;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
    }

    private boolean notEmptyOrBlank(String field) {
        return field != null && !field.isBlank();
    }

    private boolean isValidPhone(String phone) {
        try {
            Integer.parseInt(phone);
            return true;
        } catch (NumberFormatException e) {
            LOG.warning("Invalid phone number: " + phone);
            return false;
        }
    }

    private boolean isValidRole(String role) {
        String[] roles = role.split("\\|");
        try {
            for (String r : roles) {
                ROLE.valueOf(r.trim());
            }
            return true;
        } catch (IllegalArgumentException e) {
            LOG.warning("Invalid role: " + role);
            return false;
        }
    }

    public boolean isValid() {
        return notEmptyOrBlank(username)
                && notEmptyOrBlank(password)
                && notEmptyOrBlank(confirmation)
                && notEmptyOrBlank(email)
                && notEmptyOrBlank(phone)
                && notEmptyOrBlank(address)
                && notEmptyOrBlank(role)
                && isValidPhone(phone)
                && isValidRole(role)
                && email.contains("@")
                && password.equals(confirmation);
    }
}
