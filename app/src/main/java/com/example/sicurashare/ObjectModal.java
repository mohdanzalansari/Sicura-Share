package com.example.sicurashare;

import java.io.Serializable;

public class ObjectModal implements Serializable {

    String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ObjectModal(String msg) {
        this.msg = msg;
    }
}
