package org.example.api;

import org.example.model.Role;

public class ChangeUserRoleRequest {
    public String username;
    public String newRole;

    public boolean isValidRole(String newRole) {
        if (newRole == null || newRole.isBlank()) {
            return false;
        }
        try {
            Role.valueOf(newRole);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isValid() {
        return username != null && !username.isBlank()
                && isValidRole(newRole);
    }
}
