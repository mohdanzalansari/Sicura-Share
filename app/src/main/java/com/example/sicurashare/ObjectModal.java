package com.example.sicurashare;

import java.io.Serializable;

public class ObjectModal implements Serializable {

    byte[] cipertext;

    String msg;
    String time;


    String status;
    String filename;
    int filetype;
    int filesize;


    public ObjectModal(String msg) {
        this.msg = msg;
    }

    public ObjectModal(String msg, String time) {
        this.msg = msg;
        this.time = time;
    }

    public ObjectModal(String msg, String time, String status) {
        this.msg = msg;
        this.time = time;
        this.status = status;
    }

    public ObjectModal(String filename, int filetype, int filesize) {
        this.filename = filename;
        this.filetype = filetype;
        this.filesize = filesize;
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getFiletype() {
        return filetype;
    }

    public void setFiletype(int filetype) {
        this.filetype = filetype;
    }

    public int getFilesize() {
        return filesize;
    }

    public void setFilesize(int filesize) {
        this.filesize = filesize;
    }

    public ObjectModal(byte[] cipertext) {
        this.cipertext = cipertext;
    }

    public byte[] getCipertext() {
        return cipertext;
    }

    public void setCipertext(byte[] cipertext) {
        this.cipertext = cipertext;
    }
}
