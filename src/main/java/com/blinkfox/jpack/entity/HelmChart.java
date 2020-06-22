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
public class HelmChart extends BaseConfig {

    /**
     * HelmChart 仓库源码所在的文件系统中的位置，可以是绝对路径，也可以是相对路径.
     */
    private String location;

    /**
     * 要推送 helm chart 所在仓库的 API URL 地址.
     */
    private String chartRepoUrl;

    /**
     * 表示 save 导出时，是否使用本插件 Docker 构建的镜像，将其也导出到最终的镜像包中，默认为 false.
     */
    private Boolean useDockerImage;

    /**
     * 保存导出 Chart 时，导出的最终镜像的文件名称，不填写，则默认是 {@code images.tgz}.
     */
    private String saveImageFileName;

    /**
     * 需要导出的镜像名称集合，可以导出多个镜像.
     */
    private String[] saveImages;

    /**
     * chart 包的相关构建目标.
     */
    private String[] goals;

    /**
     * Registry 远程仓库的权限认证信息.
     *
     * @since v1.5.0
     */
    private RegistryUser registryUser;

}
