package com.blinkfox.jpack.handler.impl;

import com.blinkfox.jpack.consts.PlatformEnum;
import com.blinkfox.jpack.entity.Docker;
import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.handler.AbstractPackHandler;
import com.blinkfox.jpack.utils.Logger;

import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;

/**
 * Docker 下的构建、打包的处理器实现类.
 *
 * @author blinkfox on 2019/5/9.
 */
public class DockerPackHandler extends AbstractPackHandler {

    /**
     * 根据打包的相关参数进行 Docker 构建和打包的方法.
     *
     * @param packInfo 打包的相关参数实体
     */
    @Override
    public void pack(PackInfo packInfo) {
        super.packInfo = packInfo;
        super.createPlatformCommonDir(PlatformEnum.DOCKER);
        try {
            this.copyDockerfile(packInfo);
        } catch (IOException e) {
            Logger.warn("Dockerfile 文件未找到，将忽略构建 Docker 镜像!");
            return;
        }

        Logger.info("开始进行 Docker 构建和打包了.");
    }

    /**
     * 复制 Dockerfile 文件到docker平台的目录中.
     *
     * @param packInfo 包信息
     */
    private void copyDockerfile(PackInfo packInfo) throws IOException {
        Docker docker = packInfo.getDocker();
        FileUtils.copyFileToDirectory(docker == null || super.isRootPath(docker.getDockerfile())
                ? "Dockerfile" : docker.getDockerfile(), super.platformPath);
    }

}
