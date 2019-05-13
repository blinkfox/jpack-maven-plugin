package com.blinkfox.jpack.handler.impl;

import com.blinkfox.jpack.consts.PlatformEnum;
import com.blinkfox.jpack.entity.Docker;
import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.handler.AbstractPackHandler;
import com.blinkfox.jpack.utils.Logger;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ProgressMessage;
import com.spotify.docker.client.messages.RegistryAuth;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.io.RawInputStreamFacade;

/**
 * Docker 下的构建、打包的处理器实现类.
 *
 * @author blinkfox on 2019/5/9.
 */
public class DockerPackHandler extends AbstractPackHandler {

    /**
     * Docker 客户端.
     */
    private DockerClient dockerClient;

    /**
     * 镜像名称.
     */
    private String imageName;

    /**
     * 镜像包.
     */
    private String imageTar;

    /**
     * 空构造方法.
     */
    public DockerPackHandler() {}

    /**
     * Docker 构建打包的处理类.
     *
     * @param packInfo PackInfo实例
     */
    public DockerPackHandler(PackInfo packInfo) {
        super.packInfo = packInfo;
    }

    /**
     * 根据打包的相关参数进行 Docker 构建和打包的方法.
     *
     * @param packInfo 打包的相关参数实体
     */
    @Override
    public void pack(PackInfo packInfo) {
        try {
            this.createDockerClient();
            dockerClient.ping();
        } catch (DockerCertificateException | DockerException | InterruptedException e) {
            this.clean();
            Logger.warn("未检测到或开启 Docker 环境，将跳过 Docker 的构建.");
            return;
        }

        super.packInfo = packInfo;
        super.createPlatformCommonDir(PlatformEnum.DOCKER);
        try {
            this.copyDockerfile();
        } catch (IOException e) {
            Logger.warn("Dockerfile 文件未找到，将忽略构建 Docker 镜像!");
            this.clean();
            return;
        }

        try {
            this.buildImage();
        } catch (Exception e) {
            Logger.error("构建 Docker 镜像失败，将返回!", e);
        }

        this.clean();
        Logger.info("Docker 的相关操作执行完毕.");
    }

    /**
     * 复制 Dockerfile 文件到docker平台的目录中.
     */
    public void copyDockerfile() throws IOException {
        Docker docker = super.packInfo.getDocker();
        FileUtils.copyFileToDirectory(docker == null || super.isRootPath(docker.getDockerfile())
                ? "Dockerfile" : docker.getDockerfile(), super.platformPath);
    }

    /**
     * 创建 DockerClient 对象实例.
     */
    public void createDockerClient() throws DockerCertificateException {
        this.dockerClient = DefaultDockerClient.fromEnv().build();
    }

    /**
     * 构建 Docker 镜像.
     *
     * @throws InterruptedException InterruptedException
     * @throws DockerException DockerException
     * @throws IOException IOException
     */
    public void buildImage() throws InterruptedException, DockerException, IOException {
        Docker docker = super.packInfo.getDocker();
        this.imageName = docker.getRepo() + "/" + docker.getName() + ":" + docker.getTag();
        Logger.info("正在构建 " + this.imageName + " 镜像...");
        String imageId = dockerClient.build(Paths.get(super.platformPath), imageName, this::printProgress);
        Logger.info("构建 " + this.imageName + " 镜像完毕，镜像ID: " + imageId);
    }

    /**
     * 推送像 Docker 镜像.
     *
     * @throws InterruptedException InterruptedException
     * @throws DockerException DockerException
     */
    public void pushImage() throws InterruptedException, DockerException, IOException {
        // 构建 Registry 授权对象实例，并做校验.
        final String registry = super.packInfo.getDocker().getRegistry();
        Logger.info("正在校验推送镜像时需要的 registry 授权是否合法...");

        RegistryAuth auth = StringUtils.isBlank(registry)
                ? RegistryAuth.fromDockerConfig().build()
                : RegistryAuth.fromDockerConfig(registry).build();
        int statusCode = dockerClient.auth(auth);
        if (statusCode != 200) {
            Logger.warn("校验 registry 授权不通过，不能推送镜像到远程镜像仓库中.");
            return;
        }

        // 推送镜像到远程镜像仓库中
        Logger.info("正在推送 " + this.imageName + " 镜像到远程仓库中...");
        dockerClient.push(StringUtils.isBlank(registry) ? this.imageName : registry + "/" + this.imageName,
                this::printProgress, auth);
        Logger.info("推送 " + this.imageName + " 镜像到远程仓库中成功.");
    }

    /**
     * 打印 Docker 处理进度.
     *
     * @param msg msg消息
     */
    private void printProgress(ProgressMessage msg) {
        String progress = msg.progress();
        if (StringUtils.isNotBlank(progress)) {
            Logger.info(progress);
        }
    }

    /**
     * 导出 Docker 镜像.
     *
     * @throws InterruptedException InterruptedException
     * @throws DockerException DockerException
     * @throws IOException IOException
     */
    public void saveImage() throws InterruptedException, DockerException, IOException {
        Docker dockerInfo = super.packInfo.getDocker();
        this.imageTar =  dockerInfo.getName() + "-" + dockerInfo.getTag() + ".tar";
        Logger.info("正在导出 Docker 镜像包: " + this.imageTar + " ...");
        // 导出镜像为 `.tar` 文件.
        try (InputStream imageInput = dockerClient.save(this.imageName)) {
            FileUtils.copyStreamToFile(new RawInputStreamFacade(imageInput),
                    new File(super.packInfo.getHomeDir().getAbsolutePath() + File.separator + this.imageTar));
        }
        Logger.info("导出 Docker 镜像包 " + this.imageTar + " 成功.");
    }

    /**
     * 关闭 Docker Client，删除 Docker 文件夹.
     */
    public void clean() {
        if (this.dockerClient != null) {
            this.dockerClient.close();
        }

        try {
            FileUtils.forceDelete(super.platformPath);
        } catch (IOException e) {
            Logger.error("删除清除 docker 下的临时文件失败.", e);
        }
    }

    /* getter 和 setter 方法. */

    public DockerClient getDockerClient() {
        return this.dockerClient;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageTar() {
        return imageTar;
    }

}
