package com.blinkfox.jpack;

import com.blinkfox.jpack.consts.SkipErrorEnum;
import com.blinkfox.jpack.entity.CopyResource;
import com.blinkfox.jpack.entity.Docker;
import com.blinkfox.jpack.entity.HelmChart;
import com.blinkfox.jpack.entity.ImageBuildObserver;
import com.blinkfox.jpack.entity.Linux;
import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.entity.Windows;
import com.blinkfox.jpack.utils.Logger;
import com.blinkfox.jpack.utils.TimeKit;
import java.io.File;
import java.io.IOException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * 基础 Mojo 需要的一些公共属性和方法等，继承自 {@link AbstractMojo}.
 *
 * @author blinkfox on 2019-05-13.
 * @since v1.0.0
 */
public abstract class AbstractBaseMojo extends AbstractMojo {

    private static final String START = ""
            + "---------------------------------- jpack start packing... ---------------------------------\n"
            + "                                   __                          __    \n"
            + "                                  |__|______  _____     ____  |  | __\n"
            + "                                  |  |\\____ \\ \\__  \\  _/ ___\\ |  |/ /\n"
            + "                                  |  ||  |_> > / __ \\_\\  \\___ |    < \n"
            + "                              /\\__|  ||   __/ (____  / \\___  >|__|_ \\\n"
            + "                              \\______||__|         \\/      \\/      \\/ v1.5.4\n";

    /**
     * 用来存放 jpack 打包时的文件夹名称常量.
     */
    static final String HOME_DIR_NAME = "jpack";

    /**
     * JDK8 的镜像名称常量.
     */
    private static final String JDK8_IMAGE = "openjdk:8-jdk-alpine";

    /**
     * 默认需要挂载出来的数据卷字符串数组常量.
     */
    private static final String[] DEFAULT_VOLUMES = new String[] {"/tmp", "/logs"};

    /**
     * Maven 运行时的 target 目录的文件对象.
     */
    @Setter
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File targetDir;

    /**
     * Maven 项目的 `groupId`.
     */
    @Setter
    @Parameter(defaultValue = "${project.groupId}", required = true)
    private String groupId;

    /**
     * Maven 项目的 `artifactId`.
     */
    @Setter
    @Parameter(defaultValue = "${project.artifactId}", required = true)
    private String artifactId;

    /**
     * Maven 项目的版本 `version`.
     */
    @Setter
    @Parameter(defaultValue = "${project.version}", required = true)
    private String version;

    /**
     * 该 Maven 项目 pom.xml 中的 `finalName`，如果 Maven 中设置了此项，打包的名称前缀将是这个值，
     * 否则打包的名称前缀 Maven 默认就是 `artifactId + version`.
     */
    @Setter
    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    private String finalName;

    /**
     * 该 Maven 项目 pom.xml 中的版本 `description`.
     */
    @Setter
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
     * 运行 SpringBoot 程序所需要的配置文件路径，可以是相对路径或者绝对路径，可填写多个.
     */
    @Parameter(property = "configFiles")
    private String[] configFiles;

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
     * 执行过程中是否清空之前打包的目录.
     */
    @Parameter(property = "cleanPackDir", defaultValue = "true")
    private String cleanPackDir;

    /**
     * 是否生成 Windows、Linux 下默认的 bin 目录及文件，默认 true，即生成 jpack 构建的 bin 目录和文件.
     *
     * @since v1.5.4
     */
    @Parameter(property = "generateBinDir", defaultValue = "true")
    private String generateBinDir;

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
    @Getter
    @Parameter(property = "docker")
    private Docker docker;

    /**
     * Helm Chart 包的相关信息.
     *
     * @since v1.5.0
     */
    @Getter
    @Parameter(property = "helmChart")
    private HelmChart helmChart;

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

        Logger.info("--------------------- jpack has been packaged to end. [costs: "
                + TimeKit.convertTime(System.nanoTime() - start) + "] ---------------------\n");
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
        this.cleanPackDir = StringUtils.isBlank(this.cleanPackDir) ? Boolean.TRUE.toString() : this.cleanPackDir;
        PackInfo packInfo = new PackInfo()
                .setTargetDir(this.targetDir)
                .setHomeDir(this.createHomeDir())
                .setArtifactId(this.artifactId)
                .setVersion(this.version)
                .setName(this.finalName)
                .setDescription(this.description)
                .setVmOptions(this.vmOptions)
                .setProgramArgs(this.programArgs)
                .setConfigFiles(this.configFiles)
                .setSkipError(SkipErrorEnum.of(this.skipError))
                .setCleanPackDir(Boolean.TRUE.equals(Boolean.valueOf(this.cleanPackDir)))
                .setGenerateBinDir(this.generateBinDir)
                .setWindows(this.windows)
                .setLinux(this.linux)
                .setDocker(this.initDefaultDockerInfo())
                .setHelmChart(this.helmChart)
                .setExcludeFiles(this.excludeFiles)
                .setCopyResources(this.copyResources)
                .setImageBuildObserver(ImageBuildObserver
                        .of(this.helmChart != null && Boolean.TRUE.equals(this.helmChart.getUseDockerImage())));
        Logger.debug(packInfo.toString());
        return packInfo;
    }

    /**
     * 创建 jpack 主文件夹目录.
     *
     * @return jpack 目录的 file 对象
     */
    private File createHomeDir() {
        File homeDir = new File(this.targetDir + File.separator + HOME_DIR_NAME + File.separator);
        try {
            // 如果主文件夹不存在，就创建一个新文件目录.
            if (!homeDir.exists()) {
                FileUtils.forceMkdir(homeDir);
                return homeDir;
            }

            // 如果文件夹存在，且配置了清除主目录的配置（默认不配置的话，视为清除），就清空目录.
            if (Boolean.TRUE.equals(Boolean.valueOf(this.cleanPackDir))) {
                FileUtils.cleanDirectory(homeDir);
            }
        } catch (IOException e) {
            Logger.error("【创建文件 -> 失败】创建或清空 jpack 文件夹【" + homeDir.getAbsolutePath() + "】失败！请检查其中是否有文件正在使用! ", e);
        }
        return homeDir;
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
        if (StringUtils.isBlank(this.docker.getFromImage())) {
            this.docker.setFromImage(JDK8_IMAGE);
        }
        if (ArrayUtils.isEmpty(this.docker.getVolumes())) {
            this.docker.setVolumes(DEFAULT_VOLUMES);
        }
        return this.docker;
    }

}
