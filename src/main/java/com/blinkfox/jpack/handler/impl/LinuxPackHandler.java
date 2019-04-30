package com.blinkfox.jpack.handler.impl;

import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.handler.AbstractPackHandler;

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
     * 根据打包的相关参数，将该 Maven 项目打包成 Windows 中的可部署包的方法.
     *
     * @param packInfo 打包的相关参数实体
     */
    @Override
    public void pack(PackInfo packInfo) {
        super.packInfo = packInfo;
        super.platformPath = packInfo.getHomeDir().getAbsolutePath() + File.separator + LINUX_DIR_NAME;
        super.createPlatformCommonDir();
        super.copyFiles("windows/README.md", "README.md");
    }

}
