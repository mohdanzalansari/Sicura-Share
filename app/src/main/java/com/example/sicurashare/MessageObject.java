package com.example.sicurashare;

public class MessageObject {
    String msg;
    String time;
    String status;
    Boolean isprotected;

    public MessageObject(String msg, String time, String status,Boolean isprotected) {
        this.msg = msg;
        this.time = time;
        this.status = status;
        this.isprotected=isprotected;
    }

    public Boolean getIsprotected() {
        return isprotected;
    }

    public void setIsprotected(Boolean isprotected) {
        this.isprotected = isprotected;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
