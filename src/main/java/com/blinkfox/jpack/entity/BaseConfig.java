package com.blinkfox.jpack.entity;

/**
 * 基础配置信息.
 *
 * @author blinkfox on 2019/5/21.
 */
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

    /* 以下是 getter 和 setter 方法. */

    public String getVmOptions() {
        return vmOptions;
    }

    public void setVmOptions(String vmOptions) {
        this.vmOptions = vmOptions;
    }

    public String getProgramArgs() {
        return programArgs;
    }

    public void setProgramArgs(String programArgs) {
        this.programArgs = programArgs;
    }

    public String[] getConfigFiles() {
        return configFiles;
    }

    public void setConfigFiles(String[] configFiles) {
        this.configFiles = configFiles;
    }

    public CopyResource[] getCopyResources() {
        return copyResources;
    }

    public void setCopyResources(CopyResource[] copyResources) {
        this.copyResources = copyResources;
    }

    public String[] getExcludeFiles() {
        return excludeFiles;
    }

    public void setExcludeFiles(String[] excludeFiles) {
        this.excludeFiles = excludeFiles;
    }

}
