package com.blinkfox.jpack.consts;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * 跳过错误的能填的枚举值.
 *
 * @author blinkfox on 2019-05-14.
 * @since v1.1.0
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SkipErrorEnum {

    /**
     * 使用默认处理的值.
     */
    DEFAULT("default"),

    /**
     * 真.
     */
    TRUE("true"),

    /**
     * 假.
     */
    FALSE("false");

    /**
     * 代码值.
     */
    private final String code;

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
