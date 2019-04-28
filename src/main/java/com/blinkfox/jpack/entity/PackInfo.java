package com.blinkfox.jpack.entity;

import java.io.File;

/**
 * 在各个平台打包时所需要封装的参数信息的实体类.
 *
 * @author blinkfox on 2019-04-28.
 */
public class PackInfo {

    /**
     * 各平台打包的主文件目录.
     */
    private File targetDir;

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

    /* 以下是 getter 和 setter 方法. */

    public File getTargetDir() {
        return targetDir;
    }

    public PackInfo setTargetDir(File targetDir) {
        this.targetDir = targetDir;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public PackInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return this.description;
    }

    public PackInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getVmOptions() {
        return this.vmOptions;
    }

    public PackInfo setVmOptions(String vmOptions) {
        this.vmOptions = vmOptions;
        return this;
    }

    public String getProgramArgs() {
        return this.programArgs;
    }

    public PackInfo setProgramArgs(String programArgs) {
        this.programArgs = programArgs;
        return this;
    }

}
