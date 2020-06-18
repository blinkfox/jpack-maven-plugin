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
     * chart 包的相关构建目标.
     */
    private String[] goals;

}
