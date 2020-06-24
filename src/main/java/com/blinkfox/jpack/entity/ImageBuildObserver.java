package com.blinkfox.jpack.entity;

import com.blinkfox.jpack.consts.ImageBuildResultEnum;
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
     * 镜像的构建结果的枚举.
     */
    private ImageBuildResultEnum buildResult;

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

    /**
     * 设置 Docker 镜像的构建结果为 {@link ImageBuildResultEnum#UNABLE}.
     */
    public void setUnableBuildResult() {
        if (this.isEnabled() && this.buildResult == null) {
            this.buildResult = ImageBuildResultEnum.UNABLE;
        }
    }

}
