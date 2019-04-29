package com.blinkfox.jpack.handler.impl;

import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.handler.AbstractPackHandler;
import org.apache.maven.plugin.logging.Log;

import java.io.File;

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
     * LinuxPackHandler 类的构造方法.
     *
     * @param log 日志实例
     */
    public LinuxPackHandler(Log log) {
        super.log = log;
    }

    /**
     * 根据打包的相关参数，将该 Maven 项目打包成 Windows 中的可部署包的方法.
     *
     * @param packInfo 打包的相关参数实体
     */
    @Override
    public void pack(PackInfo packInfo) {
        super.platformPath = packInfo.getHomeDir().getAbsolutePath() + File.separator + LINUX_DIR_NAME;
        super.binPath = super.platformPath + File.separator + AbstractPackHandler.BIN_DIR_NAME + File.separator;
        super.createPlatformCommonDir();
    }

}
