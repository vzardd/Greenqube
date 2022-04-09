package com.vzardd.greenqube.chatclasses;

public class ChatClass {
    public String uid;
    public String type;
    public String message;
    public Object timestamp;

    public ChatClass() {
    }

    public ChatClass(String uid, String type, String message, Object timestamp) {
        this.uid = uid;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
