package com.vzardd.greenqube.friendsfragments;

public class Participant {
    String uid;
    boolean admin;

    public Participant() {
    }

    public Participant(String uid, boolean admin) {
        this.uid = uid;
        this.admin = admin;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
