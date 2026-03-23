package org.example.models;

public class User {
    private String username;
    private String password;
    private String confirm;
    private String email;
    private String phone;
    private String address;
    private String role; // USER | BOFFICER | ADMIN

    public User() {}

    public User(String username, String password, String confirm, String email, String phone, String address, String role) {
        this.username = username;
        this.password = password;
        this.confirm = confirm;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirm() {
        return confirm;
    }

    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}