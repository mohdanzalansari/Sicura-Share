package com.example.sicurashare;

import java.io.Serializable;

public class ObjectModal implements Serializable {

    byte[] cipertext;

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
