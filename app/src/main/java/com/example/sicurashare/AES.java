package com.example.sicurashare;


import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class AES {


    private SecretKeySpec generateKey(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {


        final MessageDigest digest=MessageDigest.getInstance("SHA-256");
        byte[] bytes=password.getBytes("UTF-8");
        digest.update(bytes,0,bytes.length);
        byte[] key=digest.digest();
        SecretKeySpec secretKeySpec=new SecretKeySpec(key,"AES");
        return secretKeySpec;
    }

    public String encrypt(String Data, String password) {


        try {
            SecretKeySpec key=generateKey(password);
            Cipher c=Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE,key);
            byte[] enVal=c.doFinal(Data.getBytes());
            String encryptedValue= android.util.Base64.encodeToString(enVal, android.util.Base64.DEFAULT);
            return encryptedValue;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;


    }
    public String decrypt(String outputString, String password) throws Exception {
        SecretKeySpec key=generateKey(password);
        Cipher c=Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE,key);
        byte[] decodedValue= android.util.Base64.decode(outputString, android.util.Base64.DEFAULT);
        byte[] decValue=c.doFinal(decodedValue);
        String decryptedValue=new String(decValue);
        return decryptedValue;


    }

    public   void encryptFile(final InputStream in, OutputStream out,String password) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        SecretKeySpec sks = generateKey(password);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, sks);
        CipherOutputStream cos = new CipherOutputStream(out, cipher);
        int b;
        byte[] d = new byte[1024];
        while((b = in.read(d)) != -1) {
            cos.write(d, 0, b);
        }

        cos.flush();
        cos.close();
        in.close();

    }

    public   void decryptfile(final InputStream in, OutputStream out,String password) throws Exception {


        SecretKeySpec sks = generateKey(password);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, sks);
        CipherInputStream cis = new CipherInputStream(in, cipher);
        int b;
        byte[] d = new byte[1024];
        while((b = cis.read(d)) != -1) {
            out.write(d, 0, b);
        }
        out.flush();
        out.close();
        cis.close();

    }

}

