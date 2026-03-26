package org.example.api;

import java.util.logging.Logger;

public class RegisterRequest {
    private static final Logger LOG = Logger.getLogger(RegisterRequest.class.getName());

    public String username;
    public String password;
    public String confirmation;
    public String phone;
    public String address;
    public Role role; // USER | BOFFICER | ADMIN

    public RegisterRequest() {}

    public RegisterRequest(String username, String password, String confirmation, String phone, String address, Role role) {
        this.username = username;
        this.password = password;
        this.confirmation = confirmation;
        this.phone = phone;
        this.address = address;
        this.role = role;
    }

    private boolean notEmptyOrBlank(String field) {
        if (field == null || field.isEmpty()) {
            LOG.warning("Invalid field: " + field);
            return false;
        }
        return true;
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

    private boolean isValidRole(Role role) {
        if (role == null) {
            LOG.warning("Invalid role");
            return false;
        }
        return true;
    }

    public boolean isValid() {
        return notEmptyOrBlank(username)
                && notEmptyOrBlank(password)
                && notEmptyOrBlank(confirmation)
                && notEmptyOrBlank(phone)
                && notEmptyOrBlank(address)
                && isValidPhone(phone)
                && isValidRole(role)
                && username.contains("@")
                && password.equals(confirmation);
    }
}
