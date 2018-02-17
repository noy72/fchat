package com.google.firebase.codelab.friendlychat;

/**
 * Created by noy on 2018/02/17.
 */

public class User {
    private String uid;
    private String mail;
    private String name;

    public User() {
    }

    public User(String uid, String mail, String name) {
        this.uid = uid;
        this.mail = mail;
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
