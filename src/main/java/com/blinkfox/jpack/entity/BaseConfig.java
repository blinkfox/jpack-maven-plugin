package com.blinkfox.jpack.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 基础配置信息.
 *
 * @author blinkfox on 2019-05-21.
 * @since v1.2.0
 */
@Getter
@Setter
public class BaseConfig {

    /**
     * 运行 Java 程序的 JVM 选项参数.
     */
    private String vmOptions;

    /**
     * 程序运行时需要的其他参数，会将该值写入到命令行中.
     */
    private String programArgs;

    /**
     * 运行 SpringBoot 程序所需要的配置文件路径，可以是相对路径、绝对路径或者网站资源文件.
     */
    private String[] configFiles;

    /**
     * 复制相关资源到各平台包的中的自定义配置参数.
     */
    private CopyResource[] copyResources;

    /**
     * 需要排除（即不生成）的文件或目录.
     */
    private String[] excludeFiles;

}
