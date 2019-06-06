package com.nasweibo.app.data;

import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;


public class User implements Serializable{
    private String uid;
    private String avatar;
    private String name;
    private String email;
    private boolean online = false;
    private Status status;

    public User(){}

    public User(FirebaseUser user){
        this.uid = user.getUid();
        this.avatar = user.getPhotoUrl().toString();
        this.name = user.getDisplayName();
        this.email = user.getEmail();

        Status status = new Status();
        status.setOnline(false);
        status.setOnlinestamp(System.currentTimeMillis());
        this.status = status;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
