package com.blinkfox.jpack.handler.impl;

import com.blinkfox.jpack.entity.PackInfo;

import com.blinkfox.jpack.handler.AbstractPackHandler;
import com.blinkfox.jpack.utils.TemplateKit;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Windows 下的打包处理器实现类.
 *
 * @author blinkfox on 2019-04-29.
 */
public class WindowsPackHandler extends AbstractPackHandler {

    /**
     * Windows 平台主目录的名称常量.
     */
    private static final String WINDOWS_DIR_NAME = "windows";

    /**
     * WindowsPackHandler 类的构造方法.
     *
     * @param log 日志实例
     */
    public WindowsPackHandler(Log log) {
        super.log = log;
    }

    /**
     * 根据打包的相关参数，将该 Maven 项目打包成 Windows 中的可部署包的方法.
     *
     * @param packInfo 打包的相关参数实体
     */
    @Override
    public void pack(PackInfo packInfo) {
        super.platformPath = packInfo.getHomeDir().getAbsolutePath() + File.separator + WINDOWS_DIR_NAME;
        super.createPlatformCommonDir();

        // 复制或渲染对应的资源文件到 windows 所在的打包文件夾中.
        super.copyFiles("windows/README.md", "README.md");
        String projectName = BIN_DIR_NAME + File.separator + packInfo.getName();
        this.renderWinswXml(super.platformPath + File.separator + projectName + ".xml", packInfo);
        super.copyFiles("windows/bin/winsw.exe", projectName + ".exe");
        super.copyFiles("windows/bin/winsw.exe.config", projectName + ".exe.config");
    }

    /**
     * 渲染 winsw.xml 文件中的一些模板数据，并将渲染后的数据写入到对应的 bin 目录文件中.
     *
     * @param destXml 渲染生成的目标 xml文件路径
     * @param packInfo 打包的相关参数
     */
    private void renderWinswXml(String destXml, PackInfo packInfo) {
        Map<String, Object> context = new HashMap<>(8);
        context.put("projectId", packInfo.getArtifactId());
        context.put("name", packInfo.getName());
        context.put("description", packInfo.getDescription());

        // 根据 JVM 选项参数和程序参数拼接出 arguments 的值.
        String vmOptions = StringUtils.isBlank(packInfo.getVmOptions()) ? "" : packInfo.getVmOptions() + " ";
        String args = StringUtils.isBlank(packInfo.getProgramArgs()) ? "" : " " + packInfo.getProgramArgs();
        context.put("arguments", vmOptions + "-jar ..\\" + packInfo.getFullJarName() + args);

        // 渲染出 winsw.xml 模板中的内容，并将内容写入到 bin 目录的文件中.
        String content = TemplateKit.render("windows/bin/winsw.xml", context);
        try {
            FileUtils.fileWrite(destXml, StandardCharsets.UTF_8.name(), content);
        } catch (IOException e) {
            log.error("渲染 winsw.xml 文件内容并写入 bin 目录出错!", e);
        }
        log.info("生成的内容:\n" + content);
    }

}
