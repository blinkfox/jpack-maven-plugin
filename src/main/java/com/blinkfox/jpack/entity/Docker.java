package com.blinkfox.jpack.entity;

/**
 * 构建 Docker 发布包相关的参数实体，继承自 BaseConfig.
 *
 * @author blinkfox on 2019/5/9.
 */
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
     * 获取镜像的 tar 包的名称.
     *
     * @return 名称字符串
     */
    public String getImageTarName() {
        return this.name + "-" + this.tag;
    }

    /**
     * 获取镜像名.
     *
     * @return 镜像名
     */
    public String getImageName() {
        return this.repo + "/" + this.name + ":" + this.tag;
    }

    /* getter 和 setter 方法. */

    public String getDockerfile() {
        return dockerfile;
    }

    public void setDockerfile(String dockerfile) {
        this.dockerfile = dockerfile;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getFromImage() {
        return fromImage;
    }

    public void setFromImage(String fromImage) {
        this.fromImage = fromImage;
    }

    public String getExpose() {
        return expose;
    }

    public void setExpose(String expose) {
        this.expose = expose;
    }

    public String[] getVolumes() {
        return volumes;
    }

    public void setVolumes(String[] volumes) {
        this.volumes = volumes;
    }

    public String[] getCustomCommands() {
        return customCommands;
    }

    public void setCustomCommands(String[] customCommands) {
        this.customCommands = customCommands;
    }

    public String[] getExtraGoals() {
        return extraGoals;
    }

    public void setExtraGoals(String[] extraGoals) {
        this.extraGoals = extraGoals;
    }

}
