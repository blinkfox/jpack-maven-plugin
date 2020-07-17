package com.blinkfox.jpack.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 构建 Docker 发布包相关的参数实体，继承自 {@link BaseConfig}.
 *
 * @author blinkfox on 2019-05-09.
 * @since v1.1.0
 */
@Getter
@Setter
public class Docker extends BaseConfig {

    /**
     * Dockerfile 路径地址，不配置的话，则默认与　pom.xml　在统一路径.
     */
    private String dockerfile;

    /**
     * Dockerfile 路径地址，不配置的话，则默认与　pom.xml　在统一路径.
     */
    private String registry;

    /**
     * 镜像的 repo，类似于 groupId.
     */
    private String repo;

    /**
     * 镜像名称.
     */
    private String name;

    /**
     * 镜像标签.
     */
    private String tag;

    /**
     * 引用的基础镜像.
     */
    private String fromImage;

    /**
     * 容器暴露出来的端口.
     */
    private String expose;

    /**
     * 自定义挂载的数据卷集合，不填写则默认挂载 `/tmp` 和 `/logs` 两个目录.
     */
    private String[] volumes;

    /**
     * 自定义的 Dockerfile 的命令集合，每一条就是一行，会插入到 Dockerfile 文件之中.
     */
    private String[] customCommands;

    /**
     * Docker 构建支持的额外目标，目前仅可以配置 save, push 两种，不配置的话，默认目标是只构建镜像.
     */
    private String[] extraGoals;

    /**
     * Registry 远程仓库的权限认证信息.
     *
     * @since v1.4.0
     */
    private RegistryUser registryUser;

    /**
     * 获取镜像的 tar 包的名称.
     *
     * @return 名称字符串
     */
    public String getImageTarName() {
        return this.name + "-" + this.tag + ".tar";
    }

    /**
     * 获取镜像名.
     *
     * @return 镜像名
     */
    public String getImageName() {
        return this.repo + "/" + this.name + ":" + this.tag;
    }

}
