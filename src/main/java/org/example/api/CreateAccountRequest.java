package org.example.api;

public class CreateAccountRequest extends Request {
    public String username;
    public String password;
    public String confirmation;
    public String phone;
    public String address;
    public Role role; // USER | BOFFICER | ADMIN

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{4,15}");
    }

    private boolean isValidRole(Role role) {
        return role != null;
    }

    public boolean isValid() {
        return notEmptyOrBlank(username)
                && notEmptyOrBlank(password)
                && notEmptyOrBlank(confirmation)
                && notEmptyOrBlank(address)
                && isValidPhone(phone)
                && isValidRole(role)
                && username.contains("@")
                && password.equals(confirmation);
    }
}
