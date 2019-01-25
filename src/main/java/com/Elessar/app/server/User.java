package com.Elessar.app.server;

/**
 * Created by Hans on 1/16/19.
 */
public class User {
    public static final String NAME = "name", PASSWORD = "password", PHONE = "phone", EMAIL = "email", ONLINE = "online";
    private final String userName, password, email, phoneNumber;
    private final boolean online;
    public User(String userName, String password, String email, String phoneNumber, boolean online) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.online = online;
    }

    public String getName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean getOnline() {
        return online;
    }
}
