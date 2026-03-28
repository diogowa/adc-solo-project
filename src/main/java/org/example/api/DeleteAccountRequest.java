package org.example.api;

public class DeleteAccountRequest {
    public String username;

    public boolean isValid() {
        return username != null && !username.isBlank();
    }
}
