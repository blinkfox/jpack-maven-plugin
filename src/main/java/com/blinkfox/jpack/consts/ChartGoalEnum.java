package com.blinkfox.jpack.consts;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Helm Chart 包的目标枚举类.
 *
 * @author blinkfox on 2020-06-19.
 * @since v1.5.0
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum  ChartGoalEnum {

    /**
     * 将 chart 源文件打包为 {@code .tgz} 格式的包文件.
     */
    PACKAGE("package"),

    /**
     * 将 chart 的 {@code .tgz} 格式的包推送到 Harbor 的 Helm Chart 库中.
     */
    PUSH("push"),

    /**
     * 本 SAVE 阶段依赖于 Docker 镜像的执行，会将 chart 的 {@code .tgz} 格式的包和 Docker 镜像的包和自定义的配置文件一起再打包导出成离线包.
     */
    SAVE("save");

    /**
     * 代码值.
     */
    private final String code;

    /**
     * 根据代码值的字符串找到对应的实例.
     *
     * @param codeStr 代码值字符串
     * @return ChartGoalEnum 实例
     */
    public static ChartGoalEnum of(String codeStr) {
        for (ChartGoalEnum goalEnum : ChartGoalEnum.values()) {
            if (goalEnum.code.equalsIgnoreCase(codeStr)) {
                return goalEnum;
            }
        }
        return null;
    }

}
