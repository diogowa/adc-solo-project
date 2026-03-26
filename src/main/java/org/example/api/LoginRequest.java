package org.example.api;

import org.example.resource.AuthResource;

import java.util.logging.Logger;

public class LoginRequest {
    private static final Logger LOG = Logger.getLogger(LoginRequest.class.getName());

    public String username;
    public String password;

    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    private boolean notEmptyOrBlank(String field) {
        if (field == null || field.isEmpty()) {
            LOG.warning("Invalid field: " + field);
            return false;
        }
        return true;
    }

    public boolean isValid() {
        return notEmptyOrBlank(username)
                && notEmptyOrBlank(password);
    }
}
