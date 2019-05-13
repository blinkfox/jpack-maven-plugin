package com.blinkfox.jpack;

import com.blinkfox.jpack.consts.ExceptionEnum;
import com.blinkfox.jpack.consts.PlatformEnum;
import com.blinkfox.jpack.exception.DockerPackException;
import com.blinkfox.jpack.handler.impl.DockerPackHandler;
import com.blinkfox.jpack.utils.Logger;
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
public class DockerBuildMojo extends AbstractBaseMojo {

    /**
     * Docker 构建的 Handler.
     */
    DockerPackHandler dockerHandler;

    /**
     * 执行 'docker-build' 构建的方法.
     */
    @Override
    protected void exec() {
        try {
            this.checkDockerEnv();
        } catch (DockerPackException e) {
            // 此处忽略异常，不用打印异常堆栈信息.
            Logger.error(ExceptionEnum.of(e.getMessage()).getMsg());
            return;
        }

        // 构建项目的 Docker 镜像.
        this.initAndbuildImage();
        this.dockerHandler.clean();
    }

    /**
     * 检查 Docker 环境是否符合构建的需求.
     */
    void checkDockerEnv() {
        this.dockerHandler = new DockerPackHandler(super.buildPackInfo());
        try {
            this.dockerHandler.createDockerClient();
            this.dockerHandler.getDockerClient().ping();
        } catch (DockerCertificateException | DockerException | InterruptedException e) {
            this.dockerHandler.clean();
            throw new DockerPackException(ExceptionEnum.NO_DOCKER.getCode(), e);
        }
    }

    /**
     * 初始化和构建 Docker 镜像的方法.
     */
    void initAndbuildImage() {
        this.dockerHandler.createPlatformCommonDir(PlatformEnum.DOCKER);
        try {
            this.dockerHandler.copyDockerfile();
        } catch (IOException e) {
            this.dockerHandler.clean();
            throw new DockerPackException(ExceptionEnum.NO_DOCKERFILE.getCode(), e);
        }

        try {
            this.dockerHandler.buildImage();
        } catch (Exception e) {
            this.dockerHandler.clean();
            throw new DockerPackException(ExceptionEnum.DOCKER_BUILD_EXCEPTION.getCode(), e);
        }
    }

}
