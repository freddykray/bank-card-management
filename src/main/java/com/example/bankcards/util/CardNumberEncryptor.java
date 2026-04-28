package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Компонент для шифрования и расшифровки полного номера банковской карты.
 *
 * <p>Полный номер карты относится к чувствительным данным, поэтому он не должен
 * храниться в базе данных в открытом виде. Этот компонент шифрует номер карты
 * перед сохранением и позволяет расшифровать его при необходимости внутри backend-приложения.</p>
 *
 * <p>Для шифрования используется AES в режиме GCM: {@code AES/GCM/NoPadding}.
 * GCM обеспечивает не только конфиденциальность данных, но и проверку целостности
 * зашифрованного значения.</p>
 */
@Component
public class CardNumberEncryptor {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int AES_256_KEY_LENGTH_BYTES = 32;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Создаёт encryptor с ключом, переданным из конфигурации приложения.
     *
     * <p>Ключ берётся из свойства {@code app.crypto.card-secret-key}.
     * Для AES-256 длина ключа должна быть ровно 32 байта.</p>
     *
     * @param secretKey строковое значение ключа шифрования
     * @throws IllegalArgumentException если длина ключа не равна 32 байтам
     */
    public CardNumberEncryptor(@Value("${app.crypto.card-secret-key}") String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length != AES_256_KEY_LENGTH_BYTES) {
            throw new IllegalArgumentException("Ключ карты должен быть 32 байта");
        }

        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * Генерирует случайный IV для AES-GCM.
     *
     * <p>Для каждого шифрования создаётся новый IV. Это важно, потому что
     * повторное использование одного и того же IV с тем же ключом в GCM-режиме
     * снижает безопасность шифрования.</p>
     *
     * @return случайный IV длиной {@link #IV_LENGTH_BYTES} байт
     */
    private byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);
        return iv;
    }

    /**
     * Шифрует полный номер карты.
     *
     * <p>Метод генерирует новый IV, шифрует номер карты через AES-GCM,
     * объединяет IV и зашифрованные данные, после чего возвращает результат
     * в Base64-формате. IV хранится вместе с ciphertext, так как он нужен
     * для последующей расшифровки.</p>
     *
     * @param rawCardNumber полный номер карты в открытом виде
     * @return Base64-строка, содержащая IV и зашифрованный номер карты
     * @throws IllegalStateException если произошла ошибка при шифровании
     */
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

    /**
     * Объединяет IV и зашифрованные данные в один массив байтов.
     *
     * <p>Первые {@link #IV_LENGTH_BYTES} байт занимает IV,
     * оставшаяся часть массива содержит зашифрованные данные.</p>
     *
     * @param iv IV, использованный при шифровании
     * @param encryptedData зашифрованные данные
     * @return общий массив байтов: IV + encryptedData
     */
    private byte[] combineIvAndEncryptedData(byte[] iv, byte[] encryptedData) {
        byte[] result = new byte[iv.length + encryptedData.length];

        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encryptedData, 0, result, iv.length, encryptedData.length);

        return result;
    }

    /**
     * Расшифровывает полный номер карты.
     *
     * <p>Метод декодирует Base64-строку, извлекает из неё IV и encrypted data,
     * затем выполняет расшифровку через AES-GCM.</p>
     *
     * @param encryptedCardNumber Base64-строка, содержащая IV и зашифрованный номер карты
     * @return полный номер карты в открытом виде
     * @throws IllegalStateException если произошла ошибка при расшифровке
     */
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

    /**
     * Извлекает зашифрованные данные из общего массива {@code IV + encryptedData}.
     *
     * @param encryptedWithIv общий массив байтов, содержащий IV и encrypted data
     * @return зашифрованные данные без IV
     */
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

    /**
     * Извлекает IV из общего массива {@code IV + encryptedData}.
     *
     * @param encryptedWithIv общий массив байтов, содержащий IV и encrypted data
     * @return IV, использованный при шифровании
     */
    private byte[] extractIv(byte[] encryptedWithIv) {
        byte[] iv = new byte[IV_LENGTH_BYTES];

        System.arraycopy(encryptedWithIv, 0, iv, 0, IV_LENGTH_BYTES);

        return iv;
    }
}