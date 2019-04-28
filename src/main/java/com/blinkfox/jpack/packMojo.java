package com.blinkfox.jpack;

import com.blinkfox.jpack.entity.PackInfo;

import java.io.File;

import com.blinkfox.jpack.handler.impl.LinuxPackHandler;
import com.blinkfox.jpack.handler.impl.WindowsPackHandler;
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
     * 该 Maven 项目 pom.xml 中的 `finalName`，如果 Maven 中设置了此项，打包的名称前缀将是这个值，
     * 否则打包的名称前缀默认就是 `artifactId + version`.
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
        PackInfo packInfo = new PackInfo()
                .setTargetDir(this.targetDir)
                .setName(this.finalName)
                .setDescription(this.description)
                .setVmOptions(this.vmOptions)
                .setProgramArgs(this.programArgs);
        log.info("PackInfo:" + packInfo);

        // 分别打包为 Windows 和 Linux 下的部署包.
        new WindowsPackHandler(log).pack(packInfo);
        new LinuxPackHandler(log).pack(packInfo);
    }

}
