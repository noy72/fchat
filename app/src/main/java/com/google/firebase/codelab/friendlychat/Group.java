package com.google.firebase.codelab.friendlychat;

/**
 * Created by noy on 2018/02/19.
 */

public class Group {
    String id;
    String name;

    public Group(){};

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
