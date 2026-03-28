package org.example.model;

public class Token {
    public String tokenId;
    public String username;

    public boolean isValid() {
        return tokenId != null && !tokenId.isBlank()
                && username != null && !username.isBlank();
    }
}
