package org.example.api;

public class DeleteAccountRequest extends Request {
    public String username;

    public boolean isValid() {
        return notEmptyOrBlank(username);
    }
}
