package com.vzardd.greenqube.chatclasses;

public class ChatListClass {
    public String uid;
    public String lastmessage;
    public Object timestamp;

    public ChatListClass() {
    }

    public ChatListClass(String uid, String lastmessage, Object timestamp) {
        this.uid = uid;
        this.lastmessage = lastmessage;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLastmessage() {
        return lastmessage;
    }

    public void setLastmessage(String lastmessage) {
        this.lastmessage = lastmessage;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
