package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CardNumberEncryptor {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int AES_256_KEY_LENGTH_BYTES = 32;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public CardNumberEncryptor(@Value("${app.crypto.card-secret-key}") String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != AES_256_KEY_LENGTH_BYTES) {

            throw new IllegalArgumentException("Ключ карты должен быть 32 байта");
        }
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

    }

    private byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);
        return iv;
    }

    public String encrypt(String rawCardNumber) {
        try {
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

            byte[] encryptedData = cipher.doFinal(rawCardNumber.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedWithIv = combineIvAndEncryptedData(iv, encryptedData);
            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception exception) {
            throw new IllegalStateException("Ошибка при шифровании номера карты", exception);
        }
    }

    private byte[] combineIvAndEncryptedData(byte[] iv, byte[] encryptedData) {
        byte[] result = new byte[iv.length + encryptedData.length];

        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encryptedData, 0, result, iv.length, encryptedData.length);

        return result;
    }

    public String decrypt(String encryptedCardNumber) {
        try {
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedCardNumber);

            byte[] iv = extractIv(encryptedWithIv);
            byte[] encryptedData = extractEncryptedData(encryptedWithIv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

            byte[] decryptedData = cipher.doFinal(encryptedData);

            return new String(decryptedData, StandardCharsets.UTF_8);

        } catch (Exception exception) {
            throw new IllegalStateException("Ошибка при расшифровке номера карты", exception);
        }
    }

    private byte[] extractEncryptedData(byte[] encryptedWithIv) {
        int encryptedDataLength = encryptedWithIv.length - IV_LENGTH_BYTES;
        byte[] encryptedData = new byte[encryptedDataLength];

        System.arraycopy(
                encryptedWithIv,
                IV_LENGTH_BYTES,
                encryptedData,
                0,
                encryptedDataLength
        );

        return encryptedData;
    }

    private byte[] extractIv(byte[] encryptedWithIv) {

        byte[] iv = new byte[IV_LENGTH_BYTES];

        System.arraycopy(encryptedWithIv, 0, iv, 0, IV_LENGTH_BYTES);

        return iv;

    }

}
