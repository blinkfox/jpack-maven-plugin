package com.blinkfox.jpack.utils;

import com.blinkfox.jpack.exception.EncryptException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * AES 加密工具类.
 *
 * @author chenjiayin on 2020-06-03.
 * @since v1.4.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AesKit {

    /**
     * 本插件使用的 AES 默认加解密的密钥.
     */
    private static final String DEFAULT_AES_KEY = "7A55Aj5DztePlVI68YZTfw==";

    /**
     * AES加密算法.
     */
    private static final String AES_ALGORITHM = "AES";

    /**
     * 默认的加密算法，用GCM模式化更安全.
     */
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/GCM/NoPadding";

    /**
     * AES 的 GCM 参数.
     */
    private static final GCMParameterSpec gcmParamSpec = new GCMParameterSpec(128, new byte[16]);

    /**
     * 本插件使用的 AES 默认加密方法.
     *
     * @param text 待加密的文本
     * @return Base64转码后的加密数据
     */
    public static String encrypt(String text) {
        return encrypt(DEFAULT_AES_KEY, text);
    }

    /**
     * AES 加密.
     *
     * @param secretKey 密钥
     * @param text 待加密的文本
     * @return Base64转码后的加密数据
     */
    public static String encrypt(String secretKey, String text) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec(secretKey), gcmParamSpec);
            return encodeBase64(cipher.doFinal(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new EncryptException("【jpack -> '文本加密'】通过 AES 加密原文出错！", e);
        }
    }

    /**
     * 本插件使用的 AES 默认解密方法.
     *
     * @param text 待加密的文本
     * @return Base64转码后的加密数据
     */
    public static String decrypt(String text) {
        return decrypt(DEFAULT_AES_KEY, text);
    }

    /**
     * AES 解密.
     *
     * @param secretKey 密钥
     * @param text 待解密文本
     * @return 解密后的原文文本
     */
    public static String decrypt(String secretKey, String text) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKeySpec(secretKey), gcmParamSpec);
            return new String(cipher.doFinal(decodeBase64(text)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptException("【jpack -> '文本解密'】通过 AES 解密密文出错！", e);
        }
    }

    /**
     * 获取 AES 的加密、解密的密钥.
     * <p>这里 AES 的密钥长度为 128.</p>
     *
     * @return Base64后的密钥字符串
     */
    public static String getSecretKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(AES_ALGORITHM);
            kg.init(128);
            SecretKey secretKey = kg.generateKey();
            return encodeBase64(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptException("【jpack -> '生成密钥'】生成 AES 的密钥出错！", e);
        }
    }

    /**
     * 将 AES 密钥转换为 SecretKeySpec 对象.
     *
     * @param key 密钥
     * @return SecretKeySpec 对象
     */
    private static SecretKeySpec getSecretKeySpec(String key) {
        return new SecretKeySpec(decodeBase64(key), AES_ALGORITHM);
    }

    /**
     * 将字节数组转成 Base64 的字符串.
     *
     * @param bytes 字节数组
     * @return Base64的字符串
     */
    public static String encodeBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 将 Base64 的字符串转成字节数组.
     *
     * @param s Base64的字符串
     * @return 字节数组
     */
    public static byte[] decodeBase64(String s) {
        return Base64.getDecoder().decode(s);
    }

}
