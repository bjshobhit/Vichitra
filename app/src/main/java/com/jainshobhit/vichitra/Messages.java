package com.jainshobhit.vichitra;

public class Messages {
    String message,currentTime,senderid;
    long timestamp;

    public Messages() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getSenderid() {
        return senderid;
    }

    public void setSenderid(String senderid) {
        this.senderid = senderid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Messages(String message, String currentTime, String senderid, long timestamp) {
        this.message = message;
        this.currentTime = currentTime;
        this.senderid = senderid;
        this.timestamp = timestamp;
    }
}
