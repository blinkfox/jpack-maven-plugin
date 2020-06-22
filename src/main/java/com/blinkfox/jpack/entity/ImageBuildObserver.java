package com.blinkfox.jpack.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * Docker 镜像构建状态的观察者实体类.
 *
 * @author blinkfox on 2020-06-22.
 * @since v1.5.0
 */
@Getter
@Setter
public class ImageBuildObserver {

    /**
     * 是否激活此观察者.
     */
    private boolean enabled;

    /**
     * 是否构建完成的布尔值.
     */
    private Boolean built;

    /**
     * 已经打过标签的镜像名称.
     */
    private String imageTagName;

    /**
     * 构造方法.
     *
     * @param enabled 是否激活
     */
    private ImageBuildObserver(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 基于是否使用 Docker 镜像的参数来构造 {@link ImageBuildObserver} 实例.
     *
     * @param useDockerImage 布尔值，可能为 {@code null}.
     * @return {@link ImageBuildObserver} 实例
     */
    public static ImageBuildObserver of(Boolean useDockerImage) {
        return Boolean.TRUE.equals(useDockerImage)
                ? new ImageBuildObserver(true)
                : new ImageBuildObserver(false);
    }

}
