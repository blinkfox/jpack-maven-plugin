package com.blinkfox.jpack.entity;

/**
 * 构建 Docker 发布包相关的参数实体.
 *
 * @author blinkfox on 2019/5/9.
 */
public class Docker {

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
     * Docker 构建支持的额外目标，目前仅可以配置 save, push 两种，不配置的话，默认目标是只构建镜像.
     */
    private String[] extraGoals;

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

    public String[] getExtraGoals() {
        return extraGoals;
    }

    public void setExtraGoals(String[] extraGoals) {
        this.extraGoals = extraGoals;
    }

}
