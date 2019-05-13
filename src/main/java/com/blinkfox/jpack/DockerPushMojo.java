package com.blinkfox.jpack;

import com.blinkfox.jpack.consts.ExceptionEnum;
import com.blinkfox.jpack.exception.DockerPackException;
import com.blinkfox.jpack.utils.Logger;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * 构建 Docker 镜像并将镜像推送到远程镜像仓库的 Mojo..
 *
 * @author blinkfox on 2019-05-14.
 */
@Mojo(name = "docker-push", defaultPhase = LifecyclePhase.PACKAGE)
public class DockerPushMojo extends DockerBuildMojo {

    /**
     * 执行 'docker-push' 构建的方法.
     */
    @Override
    protected void exec() {
        try {
            super.checkDockerEnv();
        } catch (DockerPackException e) {
            // 此处忽略异常，不用打印异常堆栈信息.
            Logger.error(ExceptionEnum.NO_DOCKER.getMsg());
            return;
        }

        // 初始化和构建镜像.
        super.initAndbuildImage();

        // 将镜像推送到远程镜像仓库.
        try {
            super.dockerHandler.pushImage();
        } catch (Exception  e) {
            throw new DockerPackException("jpack 推送 " + dockerHandler.getImageName() + " 镜像到远程仓库中失败！", e);
        }
        super.dockerHandler.clean();
    }

}
