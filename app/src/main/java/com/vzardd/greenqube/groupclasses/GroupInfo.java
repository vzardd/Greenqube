package com.vzardd.greenqube.groupclasses;

public class GroupInfo {
    public String key;
    public String lastmessage;
    public Object timestamp;

    public GroupInfo() {
    }

    public GroupInfo(String key, String lastmessage, Object timestamp) {
        this.key = key;
        this.lastmessage = lastmessage;
        this.timestamp = timestamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
