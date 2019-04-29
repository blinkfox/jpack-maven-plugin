package com.blinkfox.jpack.handler;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.logging.Log;
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
            // 创建或清空各平台的主目录.
            if (platformDir.exists()) {
                FileUtils.cleanDirectory(platformDir);
            } else {
                FileUtils.mkdir(this.platformPath);
            }

            // 在主目录下创建 bin, docs, logs 等目录.
            this.binPath = this.platformPath + File.separator + AbstractPackHandler.BIN_DIR_NAME + File.separator;
            FileUtils.forceMkdir(new File(binPath));
            FileUtils.forceMkdir(new File(this.platformPath + File.separator + "docs" + File.separator));
            FileUtils.forceMkdir(new File(this.platformPath + File.separator + "logs" + File.separator));
        } catch (IOException e) {
            log.error("清空【" + platformPath + "】目录或者创建 bin 目录等失败！请检查文件是否正在使用!", e);
        }
    }

}
