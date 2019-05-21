package com.blinkfox.jpack;

import com.blinkfox.jpack.consts.SkipErrorEnum;
import com.blinkfox.jpack.entity.CopyResource;
import com.blinkfox.jpack.entity.Docker;
import com.blinkfox.jpack.entity.Linux;
import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.entity.Windows;
import com.blinkfox.jpack.utils.Logger;
import com.blinkfox.jpack.utils.TimeKit;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * 基础 Mojo 需要的一些公共属性和方法等.
 *
 * @author blinkfox on 2019-05-13.
 */
public abstract class AbstractBaseMojo extends AbstractMojo {

    private static final String START = ""
            + "-------------------------- jpack start packing... -------------------------\n"
            + "                             __                          __    \n"
            + "                            |__|______  _____     ____  |  | __\n"
            + "                            |  |\\____ \\ \\__  \\  _/ ___\\ |  |/ /\n"
            + "                            |  ||  |_> > / __ \\_\\  \\___ |    < \n"
            + "                        /\\__|  ||   __/ (____  / \\___  >|__|_ \\\n"
            + "                        \\______||__|         \\/      \\/      \\/\n";

    /**
     * 用来存放 jpack 打包时的文件夹名称常量.
     */
    static final String HOME_DIR_NAME = "jpack";

    /**
     * Maven 运行时的 target 目录的文件对象.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File targetDir;

    /**
     * Maven 项目的 `groupId`.
     */
    @Parameter(defaultValue = "${project.groupId}", required = true)
    private String groupId;

    /**
     * Maven 项目的 `artifactId`.
     */
    @Parameter(defaultValue = "${project.artifactId}", required = true)
    private String artifactId;

    /**
     * Maven 项目的版本 `version`.
     */
    @Parameter(defaultValue = "${project.version}", required = true)
    private String version;

    /**
     * 该 Maven 项目 pom.xml 中的 `finalName`，如果 Maven 中设置了此项，打包的名称前缀将是这个值，
     * 否则打包的名称前缀 Maven 默认就是 `artifactId + version`.
     */
    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    private String finalName;

    /**
     * 该 Maven 项目 pom.xml 中的版本 `description`.
     */
    @Parameter(defaultValue = "${project.description}")
    private String description;

    /**
     * 运行 Java 程序时的 JVM 相关的参数.
     */
    @Parameter(property = "vmOptions")
    private String vmOptions;

    /**
     * 运行 Java 程序时的程序本身可能需要的参数.
     */
    @Parameter(property = "programArgs")
    private String programArgs;

    /**
     * 运行 SpringBoot 程序所需要的配置文件路径，可以是相对路径或者绝对路径.
     */
    @Parameter(property = "configFile")
    private String configFile;

    /**
     * 支持的打包平台数组，如果没有或者为空，则视为支持所有平台.
     */
    @Parameter(property = "platforms")
    protected String[] platforms;

    /**
     * 执行过程中是否跳过异常或错误，如果为true则直跳过不抛异常，否则抛出异常，默认值是default，会折中做了默认处理.
     */
    @Parameter(property = "skipError")
    private String skipError;

    /**
     * 构建 Docker 发布包相关的参数.
     */
    @Parameter(property = "windows")
    private Windows windows;

    /**
     * 构建 Docker 发布包相关的参数.
     */
    @Parameter(property = "linux")
    private Linux linux;

    /**
     * 构建 Docker 发布包相关的参数.
     */
    @Parameter(property = "docker")
    private Docker docker;

    /**
     * 需要排除（即不生成）的文件或目录.
     */
    @Parameter(property = "excludeFiles")
    private String[] excludeFiles;

    /**
     * 复制相关资源到各平台包的中的自定义配置参数.
     */
    @Parameter(property = "copyResources")
    private CopyResource[] copyResources;

    /**
     * Perform whatever build-process behavior this <code>Mojo</code> implements.<br>
     * This is the main trigger for the <code>Mojo</code> inside the <code>Maven</code> system, and allows
     * the <code>Mojo</code> to communicate errors.
     */
    @Override
    public void execute() {
        Logger.initSetLog(super.getLog());
        Logger.info(START);
        final long start = System.nanoTime();

        this.exec();

        Logger.info("------------- jpack has been packaged to end. [costs: "
                + TimeKit.convertTime(System.nanoTime() - start) + "] -------------\n");
    }

    /**
     * 正式执行构建的方法.
     */
    protected abstract void exec();

    /**
     * 构建 PackInfo 对象实例，便于传递复用此对象属性.
     *
     * @return PackInfo 对象实例
     */
    protected PackInfo buildPackInfo() {
        PackInfo packInfo = new PackInfo()
                .setTargetDir(this.targetDir)
                .setHomeDir(this.createHomeDir())
                .setArtifactId(this.artifactId)
                .setVersion(this.version)
                .setName(this.finalName)
                .setDescription(this.description)
                .setVmOptions(this.vmOptions)
                .setProgramArgs(this.programArgs)
                .setConfigFile(this.configFile)
                .setSkipError(SkipErrorEnum.of(this.skipError))
                .setWindows(this.windows)
                .setLinux(this.linux)
                .setDocker(this.initDefaultDockerInfo())
                .setExcludeFiles(this.excludeFiles)
                .setCopyResources(this.copyResources);
        Logger.debug(packInfo.toString());
        return packInfo;
    }

    /**
     * 创建 jpack 主文件夹目录.
     *
     * @return jpack 目录的 file 对象
     */
    private File createHomeDir() {
        File file = new File(this.targetDir + File.separator + HOME_DIR_NAME + File.separator);
        try {
            if (file.exists()) {
                FileUtils.cleanDirectory(file);
            } else {
                FileUtils.forceMkdir(file);
            }
        } catch (IOException e) {
            Logger.error("创建 jpack 文件夹失败！请检查其中是否有文件正在使用! ", e);
        }
        return file;
    }

    /**
     * 构建 Docker 镜像相关的默认信息.
     *
     * @return Docker实例
     */
    private Docker initDefaultDockerInfo() {
        if (this.docker == null) {
            this.docker = new Docker();
        }

        if (StringUtils.isBlank(this.docker.getRepo())) {
            this.docker.setRepo(this.groupId);
        }
        if (StringUtils.isBlank(this.docker.getName())) {
            this.docker.setName(this.artifactId);
        }
        if (StringUtils.isBlank(this.docker.getTag())) {
            this.docker.setTag(this.version);
        }
        return this.docker;
    }

    /* getter and setter methods. */

    void setTargetDir(File targetDir) {
        this.targetDir = targetDir;
    }

    void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    void setVersion(String version) {
        this.version = version;
    }

    void setFinalName(String finalName) {
        this.finalName = finalName;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public Docker getDocker() {
        return docker;
    }

}
