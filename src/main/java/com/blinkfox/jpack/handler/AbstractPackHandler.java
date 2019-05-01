package com.blinkfox.jpack.handler;

import com.blinkfox.jpack.consts.PlatformEnum;
import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.utils.CompressKit;
import com.blinkfox.jpack.utils.Logger;

import java.io.File;
import java.io.IOException;

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
        try {
            // 初始化资源管理器对象，用于获取 resources 下的资源.
            this.resourceManager = (ResourceManager) new DefaultPlexusContainer().lookup(ResourceManager.ROLE);

            // 创建或清空各平台的主目录.
            FileUtils.mkdir(this.platformPath);

            // 在主目录下创建 bin, docs, logs 等目录.
            this.binPath = this.platformPath + File.separator + AbstractPackHandler.BIN_DIR_NAME + File.separator;
            FileUtils.forceMkdir(new File(binPath));
            FileUtils.forceMkdir(new File(this.platformPath + File.separator + "docs"));
            FileUtils.forceMkdir(new File(this.platformPath + File.separator + "logs"));

            // 复制 target 目录中的 jar 包到各平台目录中.
            FileUtils.copyFileToDirectory(packInfo.getTargetDir().getAbsolutePath()
                    + File.separator + packInfo.getFullJarName(), platformPath);
        } catch (IOException | PlexusContainerException | ComponentLookupException e) {
            Logger.error("清空【" + platformPath + "】目录或者创建 bin 目录等失败！请检查文件是否正在使用!", e);
        }
    }

    /**
     * 复制基础文件到各平台的主目录中，如：`README.md`.
     *
     * @param source      源地址
     * @param destination 目标地址
     */
    protected void copyFiles(String source, String destination) {
        try {
            FileUtils.copyFile(this.resourceManager.getResourceAsFile(source),
                    new File(this.platformPath, destination));
        } catch (IOException | ResourceNotFoundException | FileResourceCreationException e) {
            Logger.error("复制默认资源到平台中出错！", e);
        }
    }

    /**
     * 制作 linux 下的 tar.gz 压缩包.
     */
    protected void compress(PlatformEnum platformEnum) {
        String platform = platformEnum.getCode();
        Logger.info("正在制作 " + platform + " 下的部署压缩包...");
        try {
            // 制作压缩包.
            switch (platformEnum) {
                case WINDOWS:
                    CompressKit.zip(platformPath, packInfo.getPackName() + ".zip");
                    break;
                case LINUX:
                    CompressKit.tarGz(platformPath, packInfo.getPackName() + ".tar.gz");
                    break;
                default:
                    break;
            }

            Logger.debug("正在清除 " + platform + " 临时文件....");
            FileUtils.forceDelete(platformPath);
            Logger.debug("已清除 " + platform + " 临时文件.");
        } catch (IOException e) {
            Logger.error("压缩并清除 " + platform + " 下部署的临时文件失败.", e);
        }
        Logger.info("制作 " + platform + " 下的部署压缩包完成.");
    }

}
