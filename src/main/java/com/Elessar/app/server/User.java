package com.Elessar.app.server;

/**
 * Created by Hans on 1/16/19.
 */
public class User {
    private final String userName, passoword, email, phoneNumber, online;
    public User(String userName, String passoword, String email, String phoneNumber, String online) {
        this.userName = userName;
        this.passoword = passoword;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.online = online;
    }

    public String getName() {
        return userName;
    }

    public String getPassword() {
        return passoword;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getOnline() {
        return online;
    }
}
