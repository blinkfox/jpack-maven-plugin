package com.blinkfox.jpack.handler.impl;

import com.blinkfox.jpack.consts.PlatformEnum;
import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.handler.AbstractPackHandler;
import com.blinkfox.jpack.utils.Logger;
import com.blinkfox.jpack.utils.TemplateKit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Linux 下的打包处理器实现类.
 *
 * @author blinkfox on 2019-04-29.
 */
public class LinuxPackHandler extends AbstractPackHandler {

    /**
     * Linux 平台主目录的名称常量.
     */
    private static final String LINUX_DIR_NAME = "linux";

    /**
     * 根据打包的相关参数，将该 Maven 项目打包成 Windows 中的可部署包的方法.
     *
     * @param packInfo 打包的相关参数实体
     */
    @Override
    public void pack(PackInfo packInfo) {
        super.packInfo = packInfo;
        super.platformPath = packInfo.getHomeDir().getAbsolutePath() + File.separator + LINUX_DIR_NAME;
        super.createPlatformCommonDir();
        super.copyFiles("linux/README.md", "README.md");

        // 渲染并写入对应的资源文件到 linux 所在的打包文件夾中.
        this.renderShell(packInfo);

        // 打 Linux 下 .tar.gz 的压缩包.
        super.compress(PlatformEnum.LINUX);
    }

    /**
     * 渲染 winsw.xml 文件中的一些模板数据，并将渲染后的数据写入到对应的 bin 目录文件中.
     *
     * @param packInfo 打包的相关参数
     */
    private void renderShell(PackInfo packInfo) {
        Map<String, Object> context = new HashMap<>(8);
        context.put("name", packInfo.getName());
        context.put("jarName", packInfo.getFullJarName());
        context.put("vmOptions", StringUtils.isBlank(packInfo.getVmOptions()) ? "" : packInfo.getVmOptions());
        context.put("programArgs", StringUtils.isBlank(packInfo.getProgramArgs()) ? "" : packInfo.getProgramArgs());

        // 渲染出 winsw.xml 模板中的内容，并将内容写入到 bin 目录的文件中.
        try {
            TemplateKit.renderFile("linux/bin/start.sh", context, super.binPath + File.separator + "start.sh");
            TemplateKit.renderFile("linux/bin/stop.sh", context, super.binPath + File.separator + "stop.sh");
        } catch (IOException e) {
            Logger.error("渲染 shell 模板内容并写入 bin 目录中出错！", e);
        }
    }

}
