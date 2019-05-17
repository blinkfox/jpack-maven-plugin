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
    WINDOWS("windows"),

    /**
     * linux.
     */
    LINUX("linux"),

    /**
     * docker.
     */
    DOCKER("docker");

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
     * 根据 platform 的字符串值转换为 PlatformEnum 的值.
     *
     * @param platform 平台字符串
     * @return PlatformEnum实例
     */
    public static PlatformEnum of(String platform) {
        for (PlatformEnum platformEnum : PlatformEnum.values()) {
            if (platformEnum.code.equals(platform)) {
                return platformEnum;
            }
        }
        return null;
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
