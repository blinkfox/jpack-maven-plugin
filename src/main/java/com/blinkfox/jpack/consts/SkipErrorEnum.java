package com.blinkfox.jpack.consts;

/**
 * 跳过错误的能填的枚举值.
 *
 * @author blinkfox on 2019-05-14.
 */
public enum SkipErrorEnum {

    DEFAULT("default"),

    TRUE("true"),

    FALSE("false");

    /**
     * 代码值.
     */
    private String code;

    /**
     * 构造方法.
     *
     * @param code 代码值
     */
    SkipErrorEnum(String code) {
        this.code = code;
    }

    /**
     * 根据代码值的字符串得到该代码值对应的实例.
     *
     * @param codeStr 代码值字符串
     * @return SkipErrorEnum实例
     */
    public static SkipErrorEnum of(String codeStr) {
        for (SkipErrorEnum skipErrorEnum : SkipErrorEnum.values()) {
            if (skipErrorEnum.code.equalsIgnoreCase(codeStr)) {
                return skipErrorEnum;
            }
        }
        return DEFAULT;
    }

}
