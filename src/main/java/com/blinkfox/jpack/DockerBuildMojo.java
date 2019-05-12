package com.blinkfox.jpack;

import com.blinkfox.jpack.consts.PlatformEnum;
import com.blinkfox.jpack.exception.DockerPackException;
import com.blinkfox.jpack.handler.impl.DockerPackHandler;
import com.blinkfox.jpack.utils.Logger;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

import java.io.IOException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * 仅仅是构建 Docker 镜像的 Mojo.
 *
 * @author blinkfox on 2019-05-13.
 */
@Mojo(name = "docker-build", defaultPhase = LifecyclePhase.PACKAGE)
public class DockerBuildMojo extends BaseMojo {

    /**
     * 执行 'docker-build' Mojo 的方法.
     */
    @Override
    public void execute() {
        // 初始化系统日志和打印 logo.
        super.initLogoAndPrintLogo();
        final long start = System.nanoTime();

        // 构建项目的 Docker 镜像.
        DockerPackHandler dockerHandler = new DockerPackHandler(super.buildPackInfo());
        try {
            dockerHandler.createDockerClient();
            DockerClient dockerClient =  dockerHandler.getDockerClient();
            dockerClient.ping();
        } catch (DockerCertificateException | DockerException | InterruptedException e) {
            Logger.warn("未检测到或开启 Docker 环境，将跳过 Docker 的构建.");
            return;
        }

        dockerHandler.createPlatformCommonDir(PlatformEnum.DOCKER);
        try {
            dockerHandler.copyDockerfile();
        } catch (IOException e) {
            Logger.error("Dockerfile 文件未找到，将忽略构建 Docker 镜像!", e);
            dockerHandler.clean();
            return;
        }

        try {
            dockerHandler.buildImage();
        } catch (Exception e) {
            throw new DockerPackException("jpack 构建 Docker 镜像出错！", e);
        }

        super.printEndTimeLine(start);
    }

}
