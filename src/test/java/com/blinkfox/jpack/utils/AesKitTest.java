package com.blinkfox.jpack.utils;

import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@link AesKit} 的单元测试类.
 *
 * @author chenjiayin on 2020-06-03.
 * @since v1.4.0
 */
public class AesKitTest {

    private static final int NUM = 100;

    /**
     * 测试拼接原文的字符串数组.
     */
    private static final String[] strArr = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b",
            "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w",
            "x", "y", "z", "{", "}", ":", "\"", "这", "是", "用", "于", "测", "试", "的", "中", "文", "数", "据", "。"};

    /**
     * 字符串数组的长度.
     */
    private static final int ARR_LEN = strArr.length;

    /**
     * 生成原文.
     *
     * @return 原文
     */
    private String generatePlainText() {
        // 拼接需要测试的随机字符串原文，字符串长度为 100~200.
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = new Random().nextInt(NUM) + NUM; i < len; i++) {
            sb.append(strArr[new Random().nextInt(ARR_LEN - 1)]);
        }
        return sb.toString();
    }

    /**
     * Success: 测试获取密钥以及根据密钥进行加密和解密成功时的情况.
     */
    @Test
    public void encryptAndDecrypt() {
        String secretKey = AesKit.getSecretKey();
        Assert.assertEquals(24, secretKey.length());
        String text = this.generatePlainText();
        String encryptText = AesKit.encrypt(secretKey, text);
        Assert.assertEquals(text, AesKit.decrypt(secretKey, encryptText));
    }

    /**
     * Success: 循环 100 次对随机字符串来测试加密、解密的正确性.
     */
    @Test
    public void loopEncryptAndDecrypt() {
        for (int i = 0; i < NUM; i++) {
            String secretKey = AesKit.getSecretKey();
            String text = this.generatePlainText();
            Assert.assertEquals(text, AesKit.decrypt(secretKey, AesKit.encrypt(secretKey, text)));
        }
    }

    @Test
    public void encodeAndDecodeBase64() {
        String text = "你好，世界!";
        Assert.assertEquals(text, new String(AesKit.decodeBase64(AesKit.encodeBase64(text.getBytes()))));
    }

    /**
     * 测试默认的加密和解密的情况.
     */
    @Test
    public void encryptAndDecryptDefault() {
        String text = "This is my text.";
        Assert.assertEquals(text, AesKit.decrypt(AesKit.encrypt(text)));
    }

}
