package com.blinkfox.jpack.consts;

/**
 * Docker 镜像构建结果的枚举类.
 *
 * @author blinkfox on 2020-06-24.
 * @since v1.5.2
 */
public enum ImageBuildResultEnum {

    /**
     * 不能进行构建，如：缺少 Docker 环境或 Docker 环境无法使用等.
     */
    UNABLE,

    /**
     * 构建成功.
     */
    SUCCESS,

    /**
     * 构建失败.
     */
    FAILURE;

}
