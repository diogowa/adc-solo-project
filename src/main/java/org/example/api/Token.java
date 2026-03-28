package org.example.api;

public class Token {
    public String tokenId;

    public boolean isValid() {
        return tokenId != null && !tokenId.isBlank();
    }
}
