package org.example.model;

public class Token {
    public String tokenId;

    public boolean isValid() {
        return tokenId != null && !tokenId.isBlank();
    }
}
