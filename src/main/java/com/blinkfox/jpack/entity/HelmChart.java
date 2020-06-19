package com.blinkfox.jpack.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 用于制作 Helm Chart 包相关的实体类.
 *
 * @author blinkfox on 2020-06-18.
 * @since v1.5.0
 */
@Getter
@Setter
public class HelmChart {

    /**
     * HelmChart 仓库源码所在的文件系统中的位置，可以是绝对路径，也可以是相对路径.
     */
    private String location;

    /**
     * 要推送 helm chart 所在仓库的 API URL 地址.
     */
    private String chartRepoUrl;

    /**
     * 需要导出的镜像名称集合，可以导出多个镜像.
     */
    private String[] saveImages;

    /**
     * chart 包的相关构建目标.
     */
    private String[] goals;

}
