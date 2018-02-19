package com.google.firebase.codelab.friendlychat;

/**
 * Created by noy on 2018/02/19.
 */

public class Group {
    long id;
    String name;
    String joinUser;

    public Group(long id, String name, String joinUser) {
        this.id = id;
        this.name = name;
        this.joinUser = joinUser;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJoinUser() {
        return joinUser;
    }

    public void setJoinUser(String joinUser) {
        this.joinUser = joinUser;
    }
}
