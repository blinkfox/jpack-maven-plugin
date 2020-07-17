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
 * Windows 下的打包处理器实现类.
 *
 * @author blinkfox on 2019-04-29.
 * @since v1.0.0
 */
public class WindowsPackHandler extends AbstractPackHandler {

    /**
     * bat 文件的名称数组.
     */
    private static final String[] BAT_NAME_ARR = {"install", "uninstall", "start", "stop", "restart", "status"};

    /**
     * 根据打包的相关参数，将该 Maven 项目打包成 Windows 中的可部署包的方法.
     *
     * @param packInfo 打包的相关参数实体
     */
    @Override
    public void pack(PackInfo packInfo) {
        super.packInfo = packInfo;
        super.createPlatformCommonDir(PlatformEnum.WINDOWS);
        super.createBaseDirs();

        // 复制或渲染对应的资源文件到 windows 所在的打包文件夾中.
        super.copyFiles("windows/README.md", "README.md");
        String projectName = BIN_DIR_NAME + File.separator + packInfo.getName();
        this.renderWinswXml(super.platformPath + File.separator + projectName + ".xml", packInfo);
        super.copyFiles("windows/bin/winsw.exe", projectName + ".exe");
        super.copyFiles("windows/bin/winsw.exe.config", projectName + ".exe.config");

        // 创建所有的 `.bat` 文件.
        this.createAllBatFiles(packInfo.getName());

        // 制作 .zip 压缩包.
        super.compress(PlatformEnum.WINDOWS);
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
        context.put("vmOptions", StringUtils.isBlank(packInfo.getVmOptions()) ? "" : packInfo.getVmOptions());
        context.put("jarName", "%BASE%\\..\\" + packInfo.getFullJarName());
        String programArgs = packInfo.getProgramArgs();
        context.put("programArgs", StringUtils.isBlank(programArgs) ? "" : " " + programArgs);

        // 渲染出 winsw.xml 模板中的内容，并将内容写入到 bin 目录的文件中.
        try {
            TemplateKit.renderFile("windows/bin/winsw.xml", context, destXml);
        } catch (IOException e) {
            Logger.error("【生成文件 -> 出错】渲染 winsw.xml 模板内容并写入 bin 目录中出错！", e);
        }
    }

    /**
     * 创建所有的 bin 目录下的 bat 文件，如：install.bat, start.bat 等.
     *
     * @param name 打包的项目名称
     */
    private void createAllBatFiles(String name) {
        try {
            for (String batName : BAT_NAME_ARR) {
                // 构造渲染的上下文参数.
                Map<String, Object> context = new HashMap<>(4);
                context.put("name", name);
                context.put("batName", batName);

                // 渲染 .bat 的模板，并将结果写入到 bin 目录下，生成如：install.bat, start.bat 等文件.
                TemplateKit.renderFile("windows/bin/template.bat", context,
                        super.binPath + batName + ".bat");
            }
        } catch (IOException e) {
            Logger.error("【生成文件 -> 出错】渲染 template.bat 模板内容并写入到 bin 目录中出错！", e);
        }
    }

}
