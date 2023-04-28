package com.akouma.veyuzwebapp.utils;


import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

@Component
public class CryptoUtils {

    private static final String ALGORITHM = "AES";
    private static final String key = "MySecretKey12345";

    public static String encrypt(Long value) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        byte[] encryptedValue = cipher.doFinal(String.valueOf(value).getBytes());
        return Base64.encodeBase64URLSafeString(encryptedValue);
    }

    public static Long decrypt(String encryptedValue) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byte[] decodedValue = Base64.decodeBase64(encryptedValue);
        byte[] decryptedValue = cipher.doFinal(decodedValue);
        return Long.valueOf(new String(decryptedValue));
    }

}
