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

    /* getter 方法. */

    public String getDockerfile() {
        return dockerfile;
    }

}
