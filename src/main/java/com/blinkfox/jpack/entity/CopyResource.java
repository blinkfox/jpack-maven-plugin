package com.blinkfox.jpack.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 某个复制资源相关属性的实体类.
 *
 * @author blinkfox on 2019-05-03.
 * @since v1.0.0
 */
@Getter
@Setter
public class CopyResource {

    /**
     * 从哪里复制的源资源路径，可以是资源，也可以是目录.
     */
    private String from;

    /**
     * 要复制到哪里的资源路径，只能是目录.
     */
    private String to;

}
