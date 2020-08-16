package com.example.firebasedemo.modules;

import java.io.Serializable;

public class User implements Serializable {
    public String username, password, email, phone;
    public boolean isInfected;

    // DO NOT REMOVE == User By Firebase
    public User() {
    }

    public User(String username, String password, String email, String phone, boolean isInfected) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.isInfected = isInfected;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", isInfected=" + isInfected +
                '}';
    }
}
