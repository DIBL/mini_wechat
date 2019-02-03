package com.Elessar.app.server;

/**
 * Created by Hans on 1/16/19.
 */
public class User {
    public static final String NAME = "name", PASSWORD = "password", PHONE = "phone", EMAIL = "email", ONLINE = "online", URL = "url";
    private final String userName, password, email, phoneNumber, url;
    private final Boolean online;
    public User(String userName, String password, String email, String phoneNumber, String url, Boolean online) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.url = url;
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

    public String getURL() {
        return url;
    }

    public Boolean getOnline() {
        return online;
    }
}
