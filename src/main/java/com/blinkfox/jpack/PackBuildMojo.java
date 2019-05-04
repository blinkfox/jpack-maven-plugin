package com.blinkfox.jpack;

import com.blinkfox.jpack.entity.CopyResource;
import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.handler.PlatformPackContext;
import com.blinkfox.jpack.utils.Logger;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;

/**
 * 打包的 Maven 插件主入口 Mojo 类.
 *
 * @author blinkfox on 2019-04-28.
 */
@Mojo(name = "build", defaultPhase = LifecyclePhase.PACKAGE)
public class PackBuildMojo extends AbstractMojo {

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
     * Maven 项目的 `artifactId`.
     */
    @Parameter(defaultValue = "${project.artifactId}", required = true)
    private String artifactId;

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
     * 支持的打包平台数组，如果没有或者为空，则视为支持所有平台.
     */
    @Parameter(property = "platforms")
    private String[] platforms;

    /**
     * 复制相关资源到各平台包的中的自定义配置参数.
     */
    @Parameter(property = "copyResources")
    private CopyResource[] copyResources;

    /**
     * 执行该 Mojo 的方法.
     */
    @Override
    public void execute() {
        Logger.initSetLog(super.getLog());
        PackInfo packInfo = new PackInfo()
                .setTargetDir(this.targetDir)
                .setHomeDir(this.createHomeDir())
                .setArtifactId(this.artifactId)
                .setName(this.finalName)
                .setDescription(this.description)
                .setVmOptions(this.vmOptions)
                .setProgramArgs(this.programArgs)
                .setCopyResources(this.copyResources);
        Logger.info(packInfo.toString());

        // 在各平台下执行打包.
        new PlatformPackContext().pack(platforms, packInfo);
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

    /* getter and setter methods. */

    void setTargetDir(File targetDir) {
        this.targetDir = targetDir;
    }

    void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    void setFinalName(String finalName) {
        this.finalName = finalName;
    }

    void setDescription(String description) {
        this.description = description;
    }

}
