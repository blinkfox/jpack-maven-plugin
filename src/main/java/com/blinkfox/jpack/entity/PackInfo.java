package com.blinkfox.jpack.entity;

/**
 * 在各个平台打包时所需要封装的参数信息的实体类.
 *
 * @author blinkfox on 2019-04-28.
 */
public class PackInfo {

    /**
     * 各平台打包的主文件目录.
     */
    private String homeDir;

    /**
     * 所打包项目的名称.
     */
    private String name;

    /**
     * 所打包项目的描述信息.
     */
    private String description;

    /**
     * 运行 Java 程序的 JVM 选项参数.
     */
    private String vmOptions;

    /**
     * 程序运行时需要的其他参数.
     */
    private String programArgs;

}
