package com.vzardd.greenqube;

public class UserClass {
    public String uid;
    public String name;
    public String bio;

    public UserClass() {
    }

    public UserClass(String uid, String name, String bio) {
        this.uid = uid;
        this.name = name;
        this.bio = bio;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
