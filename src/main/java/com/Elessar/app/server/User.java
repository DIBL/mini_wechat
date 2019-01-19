package com.Elessar.app.server;

/**
 * Created by Hans on 1/16/19.
 */
public class User {
    private final String userName, passowordHash, email, phoneNumber;
    boolean online;
    public User(String userName, String passowordHash, String email, String phoneNumber, boolean online) {
        this.userName = userName;
        this.passowordHash = passowordHash;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.online = online;
    }

    public String getPassword() {
        return passowordHash;
    }

    public void setOnline() {
        online = true;
    }

    public void setOffline() {
        online = false;
    }

    public boolean getOnlineStatus() {
        return online;
    }
}
