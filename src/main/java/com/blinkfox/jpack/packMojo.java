package com.blinkfox.jpack;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * 打包的 Maven 插件主入口 Mojo 类.
 *
 * @author blinkfox on 2019-04-28.
 */
@Mojo(name = "pack", defaultPhase = LifecyclePhase.PACKAGE)
public class packMojo extends AbstractMojo {

    /**
     * 日志对象.
     */
    private static Log log;

    /**
     * Maven 运行时的 target 目录的文件对象.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File targetDir;

    /**
     * 该 Maven 项目 pom.xml 中的 `artifactId`.
     */
    @Parameter(defaultValue = "${project.artifactId}", required = true)
    private String artifactId;

    /**
     * 该 Maven 项目 pom.xml 中的版本 `version`.
     */
    @Parameter(defaultValue = "${project.version}", required = true)
    private String version;

    /**
     * 该 Maven 项目 pom.xml 中的 `finalName`，如果 Maven 中设置了此项，打包的名称前缀将是这个值，
     * 否则打包的名称前缀就是 `artifactId + version`.
     */
    @Parameter(defaultValue = "${project.build.finalName}")
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
     * 执行该 Mojo 的方法.
     */
    @Override
    public void execute() {
        log = getLog();
        log.info("---------- 开始执行打包... -----------");
        log.info("targetDir: " + targetDir);
        log.info("artifactId: " + artifactId);
        log.info("version: " + version);
        log.info("finalName: " + finalName);
        log.info("description: " + description);
        log.info("vmOptions: " + vmOptions);
        log.info("programArgs: " + programArgs);
        log.info("---------- 执行打包结束. -------------");
    }

}
