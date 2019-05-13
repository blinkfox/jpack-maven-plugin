package com.blinkfox.jpack;

import com.blinkfox.jpack.consts.ExceptionEnum;
import com.blinkfox.jpack.exception.DockerPackException;
import com.blinkfox.jpack.utils.Logger;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * 构建 Docker 镜像并将镜像导出为离线的 .tar 包的 Mojo.
 *
 * @author blinkfox on 2019-05-13.
 */
@Mojo(name = "docker-save", defaultPhase = LifecyclePhase.PACKAGE)
public class DockerSaveMojo extends DockerBuildMojo {

    /**
     * 执行 'docker-save' 构建的方法.
     */
    @Override
    protected void exec() {
        try {
            super.checkDockerEnv();
        } catch (DockerPackException e) {
            // 此处忽略异常，不用打印异常堆栈信息.
            Logger.error(ExceptionEnum.of(e.getMessage()).getMsg());
            return;
        }

        // 初始化和构建镜像.
        super.initAndbuildImage();

        // 导出镜像为 tar 包.
        try {
            super.dockerHandler.saveImage();
        } catch (Exception  e) {
            throw new DockerPackException("jpack 导出 " + dockerHandler.getImageTar() + " 的镜像包出错！", e);
        }
        super.dockerHandler.clean();
    }

}
