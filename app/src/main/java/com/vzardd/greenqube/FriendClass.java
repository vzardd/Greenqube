package com.vzardd.greenqube;

public class FriendClass {
    public String status;
    public String uid;

    public FriendClass() {
    }

    public FriendClass(String status, String uid) {
        this.status = status;
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
