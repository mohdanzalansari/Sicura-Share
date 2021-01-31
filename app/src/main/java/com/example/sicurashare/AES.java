package com.example.sicurashare;


import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
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

}

