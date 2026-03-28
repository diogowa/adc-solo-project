package org.example.api;

import org.example.model.Role;

public class ChangeUserRoleRequest {
    public String username;
    public Role newRole;

    public boolean isValid() {
        return username != null && !username.isBlank()
                && newRole != null;
    }
}
