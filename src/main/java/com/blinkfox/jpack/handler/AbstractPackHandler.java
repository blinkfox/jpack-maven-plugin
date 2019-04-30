package com.blinkfox.jpack.handler;

import java.io.File;
import java.io.IOException;

import com.blinkfox.jpack.entity.PackInfo;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.resource.ResourceManager;
import org.codehaus.plexus.resource.loader.FileResourceCreationException;
import org.codehaus.plexus.resource.loader.ResourceNotFoundException;
import org.codehaus.plexus.util.FileUtils;

/**
 * 各个平台可公用的、抽象的打包处理器抽象类.
 *
 * @author blinkfox on 2019-04-29.
 */
public abstract class AbstractPackHandler implements PackHandler {

    /**
     * bin 主目录的名称常量.
     */
    protected static final String BIN_DIR_NAME = "bin";

    /**
     * 资源管理器对象.
     */
    private ResourceManager resourceManager;

    /**
     * 打包的相关参数实体对象.
     */
    protected PackInfo packInfo;

    /**
     * Maven 插件可公用的日志对象.
     */
    protected Log log;

    /**
     * 各平台中的主目录路径.
     */
    protected String platformPath;

    /**
     * 各平台中的主目录中的 bin 目录路径.
     */
    protected String binPath;

    /**
     * 创建各个平台下的主目录和主目录中的 bin, docs, logs 等目录文件夹.
     */
    protected void createPlatformCommonDir() {
        File platformDir = new File(this.platformPath);
        try {
            // 初始化资源管理器对象，用于获取 resources 下的资源.
            this.resourceManager = (ResourceManager) new DefaultPlexusContainer().lookup(ResourceManager.ROLE);

            // 创建或清空各平台的主目录.
            if (platformDir.exists()) {
                FileUtils.cleanDirectory(platformDir);
            } else {
                FileUtils.mkdir(this.platformPath);
            }

            // 在主目录下创建 bin, docs, logs 等目录.
            this.binPath = this.platformPath + File.separator + AbstractPackHandler.BIN_DIR_NAME + File.separator;
            FileUtils.forceMkdir(new File(binPath));
            FileUtils.forceMkdir(new File(this.platformPath + File.separator + "docs"));
            FileUtils.forceMkdir(new File(this.platformPath + File.separator + "logs"));
        } catch (IOException | PlexusContainerException | ComponentLookupException e) {
            log.error("清空【" + platformPath + "】目录或者创建 bin 目录等失败！请检查文件是否正在使用!", e);
        }
    }

    /**
     * 复制基础文件到各平台的主目录中，如：`README.md`.
     *
     * @param source 源地址
     * @param destination 目标地址
     */
    protected void copyFiles(String source, String destination) {
        try {
            FileUtils.copyFile(this.resourceManager.getResourceAsFile(source),
                    new File(this.platformPath, destination));
        } catch (IOException | ResourceNotFoundException | FileResourceCreationException e) {
            log.error("复制默认资源到平台中出错！", e);
        }
    }

}
