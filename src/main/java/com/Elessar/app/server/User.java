package com.Elessar.app.server;

/**
 * Created by Hans on 1/16/19.
 */
public class User {
    private String userName, password, email, phoneNumber;
    boolean online;
    public User(String userName, String password, String email, String phoneNumber, boolean online) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.online = online;
    }

    public String getPassword() {
        return password;
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
