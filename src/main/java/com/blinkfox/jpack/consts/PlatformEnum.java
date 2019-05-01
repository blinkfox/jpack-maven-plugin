package com.blinkfox.jpack.consts;

/**
 * 平台的枚举类.
 *
 * @author blinkfox on 2019-05-01.
 */
public enum PlatformEnum {

    /**
     * windows.
     */
    WINDOWS("Windows"),

    /**
     * linux.
     */
    LINUX("Linux");

    /**
     * 属性值.
     */
    private String code;

    /**
     * 构造方法.
     *
     * @param code code值
     */
    PlatformEnum(String code) {
        this.code = code;
    }

    /**
     * 获取该平台的 code 值.
     *
     * @return code
     */
    public String getCode() {
        return code;
    }

}
