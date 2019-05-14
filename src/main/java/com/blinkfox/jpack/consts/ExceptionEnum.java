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
    NO_DOCKER("未检测到或开启 Docker 环境."),

    /**
     * 没有 Dockerfile 文件的枚举实例.
     */
    NO_DOCKERFILE("Dockerfile 文件未找到，构建 Docker 镜像失败!"),

    /**
     * 使用 jpack 构建 Docker 镜像出错的枚举实例.
     */
    DOCKER_BUILD_EXCEPTION("jpack 构建 Docker 镜像失败！"),

    /**
     * 使用 jpack 导出 Docker 镜像出错的枚举实例.
     */
    DOCKER_SAVE_EXCEPTION("jpack 导出 Docker 镜像失败！"),

    /**
     * 使用 jpack 推送 Docker 镜像出错的枚举实例.
     */
    DOCKER_PUSH_EXCEPTION("jpack 推送 Docker 镜像失败！");

    /**
     * 异常的描述信息.
     */
    private String msg;

    /**
     * 构造方法.
     *
     * @param msg 异常的描述信息
     */
    ExceptionEnum(String msg) {
        this.msg = msg;
    }

    /**
     * 获取异常的描述信息值.
     *
     * @return 描述信息
     */
    public String getMsg() {
        return this.msg;
    }

}
