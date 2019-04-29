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
     * 创建各个平台下的 home 目录和 home 目录中的 bin 目录文件夹.
     */
    protected void createHomeAndBinDir() {
        File platformDir = new File(this.platformPath);
        try {
            if (platformDir.exists()) {
                FileUtils.cleanDirectory(platformDir);
            } else {
                FileUtils.mkdir(this.platformPath);
            }
            FileUtils.forceMkdir(new File(binPath));
        } catch (IOException e) {
            log.error("清空【" + platformPath + "】目录或者创建 bin 目录失败！请检查文件是否正在使用!", e);
        }
    }

}
