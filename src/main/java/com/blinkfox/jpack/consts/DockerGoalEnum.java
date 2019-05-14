package com.blinkfox.jpack.consts;

/**
 * Docker 构建目标的枚举类.
 *
 * @author blinkfox on 2019-05-14.
 */
public enum DockerGoalEnum {

    /**
     * 导出镜像到本地为 '.tar' 文件.
     */
    SAVE("save"),

    /**
     * 推送镜像到远程仓库中.
     */
    PUSH("push");

    /**
     * 代码值.
     */
    private String code;

    /**
     * 构造方法.
     *
     * @param code 代码值
     */
    DockerGoalEnum(String code) {
        this.code = code;
    }

    /**
     * 根据代码值的字符串找到对应的实例.
     *
     * @param codeStr 代码值字符串
     * @return DockerGoalEnum实例
     */
    public static DockerGoalEnum of(String codeStr) {
        for (DockerGoalEnum goalEnum : DockerGoalEnum.values()) {
            if (goalEnum.code.equalsIgnoreCase(codeStr)) {
                return goalEnum;
            }
        }
        return null;
    }

}
