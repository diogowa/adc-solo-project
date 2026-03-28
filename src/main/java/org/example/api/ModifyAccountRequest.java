package org.example.api;

public class ModifyAccountRequest {
    public String username;
    public Attributes attributes;

    public static class Attributes {
        public String phone;
        public String address;
    }

    public boolean isValid() {
        return username != null && !username.isBlank()
                && attributes != null
                && attributes.phone != null && !attributes.phone.isBlank()
                && attributes.address != null && !attributes.address.isBlank();
    }
}
