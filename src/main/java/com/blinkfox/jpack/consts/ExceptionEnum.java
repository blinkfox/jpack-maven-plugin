package com.blinkfox.jpack.consts;

/**
 * 异常信息标识和提示信息的对应关系枚举类.
 *
 * @author blinkfox on 2019-05-13.
 */
public enum ExceptionEnum {

    /**
     * 没有 Docker 环境的枚举实例.
     */
    NO_DOCKER("no_docker", "未检测到或开启 Docker 环境，将跳过 Docker 的构建."),

    /**
     * 没有 Dockerfile 文件的枚举实例.
     */
    NO_DOCKERFILE("no_dockerfile", "Dockerfile 文件未找到，将忽略构建 Docker 镜像!"),

    /**
     * 使用 jpack 构建 Docker 镜像出错的枚举实例.
     */
    DOCKER_BUILD_EXCEPTION("docker_build_exception", "jpack 构建 Docker 镜像出错！"),

    /**
     * 使用 jpack 导出 Docker 镜像出错的枚举实例.
     */
    DOCKER_SAVE_EXCEPTION("docker_save_exception", "jpack 导出 Docker 镜像出错！"),

    /**
     * 一个通用的 jpack 执行异常.
     */
    OTHER_EXCEPTION("other_exception", "jpack 执行异常.");

    /**
     * 异常类型的代码值.
     */
    private String code;

    /**
     * 异常的描述信息.
     */
    private String msg;

    /**
     * 构造方法.
     *
     * @param code 异常类型的代码值
     * @param msg 异常的描述信息
     */
    ExceptionEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 根据 code 的字符串值查找到枚举的实例.
     *
     * @param codeStr codeStr
     * @return ExceptionEnum实例
     */
    public static ExceptionEnum of(String codeStr) {
        for (ExceptionEnum exceptionEnum : ExceptionEnum.values()) {
            if (exceptionEnum.code.equals(codeStr)) {
                return exceptionEnum;
            }
        }
        return OTHER_EXCEPTION;
    }

    /**
     * 获取代码值.
     *
     * @return 代码值
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 获取描述信息值.
     *
     * @return 描述信息
     */
    public String getMsg() {
        return this.msg;
    }

}
