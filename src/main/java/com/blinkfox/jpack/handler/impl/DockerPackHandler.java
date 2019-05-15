package com.blinkfox.jpack.handler.impl;

import com.blinkfox.jpack.consts.DockerGoalEnum;
import com.blinkfox.jpack.consts.ExceptionEnum;
import com.blinkfox.jpack.consts.PlatformEnum;
import com.blinkfox.jpack.consts.SkipErrorEnum;
import com.blinkfox.jpack.entity.Docker;
import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.exception.DockerPackException;
import com.blinkfox.jpack.handler.AbstractPackHandler;
import com.blinkfox.jpack.utils.Logger;
import com.blinkfox.jpack.utils.TemplateKit;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ProgressMessage;
import com.spotify.docker.client.messages.RegistryAuth;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

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
     * Dockerfile 配置文件的名称常量.
     */
    private static final String DOCKER_FILE = "Dockerfile";

    /**
     * Docker 客户端.
     */
    private DockerClient dockerClient;

    /**
     * 镜像名称.
     */
    private String imageName;

    /**
     * 根据打包的相关参数进行 Docker 构建和打包的方法.
     *
     * <p>注意：这里区分是否抛出异常的情况，如果未配置是否跳过异常，则使用我的默认处理方式.</p>
     *
     * @param packInfo 打包的相关参数实体
     */
    @Override
    public void pack(PackInfo packInfo) {
        super.packInfo = packInfo;

        // 针对需要打印异常地方，需要再 try-catch-finally 一次，有异常需要抛，最后关闭 dockerClient 等.
        try {
            this.doPack();
        } catch (Exception e) {
            Logger.error("jpack 执行 Docker 构建失败！");
            throw e;
        } finally {
            this.clean();
        }
    }

    private void doPack() {
        // 如果 SkipError 为true，则需要 try-catch 住，且发生异常均要捕捉，而不抛出异常.
        if (SkipErrorEnum.TRUE == packInfo.getSkipError()) {
            try {
                this.doBuild();
                this.printFinished();
            } catch (Exception e) {
                // 此处忽略异常堆栈信息，只打印错误信息，但不打印异常堆栈信息.
                Logger.error(e.getMessage());
            }
            return;
        }

        // 如果 SkipError 为 false，则意味着一旦遇到异常，则需要抛出异常，不做 try-catch 处理.
        if (SkipErrorEnum.FALSE == packInfo.getSkipError()) {
            this.doBuild();
            this.printFinished();
            return;
        }

        // 最后 skipError 不配置的话，则走默认的处理方式，如果 未检测到 docker 环境，则直接跳过构建.
        // 如果有 docker环境，则正常构建，发生异常的话抛出异常.
        try {
            this.checkDockerEnv();
        } catch (Exception e) {
            // 此处忽略异常堆栈信息，不用打印异常堆栈信息.
            Logger.error(e.getMessage());
            return;
        }

        this.doBuildWithoutCheck();
        this.printFinished();
    }

    /**
     * 检查 Docker 环境是否符合构建的需求.
     */
    private void checkDockerEnv() {
        try {
            this.dockerClient = DefaultDockerClient.fromEnv().build();
            this.dockerClient.ping();
        } catch (Exception e) {
            throw new DockerPackException(ExceptionEnum.NO_DOCKER.getMsg(), e);
        }
    }

    /**
     * 初始化 Dockerfile 文件和复制出 jar 包.
     */
    private void initDockerfileAndJar() {
        super.createPlatformCommonDir(PlatformEnum.DOCKER);
        try {
            this.copyDockerfile();
        } catch (IOException e) {
            throw new DockerPackException(ExceptionEnum.NO_DOCKERFILE.getMsg(), e);
        }
    }

    /**
     * 构建该服务的 Docker 镜像.
     */
    private void buildImage() {
        try {
            Docker docker = super.packInfo.getDocker();
            this.imageName = docker.getRepo() + "/" + docker.getName() + ":" + docker.getTag();
            Logger.info("正在构建 " + this.imageName + " 镜像...");
            String imageId = dockerClient.build(Paths.get(super.platformPath), imageName, this::printProgress);
            Logger.info("构建 " + this.imageName + " 镜像完毕，镜像ID: " + imageId);
        } catch (Exception e) {
            throw new DockerPackException(ExceptionEnum.DOCKER_BUILD_EXCEPTION.getMsg(), e);
        }
    }

    /**
     * 导出该服务的镜像为 '.tar' 包.
     */
    private void saveImage() {
        try {
            Docker dockerInfo = super.packInfo.getDocker();
            String imageTar =  dockerInfo.getName() + "-" + dockerInfo.getTag() + ".tar";
            Logger.info("正在导出 Docker 镜像包: " + imageTar + " ...");
            // 导出镜像为 `.tar` 文件.
            try (InputStream imageInput = dockerClient.save(this.imageName)) {
                FileUtils.copyStreamToFile(new RawInputStreamFacade(imageInput),
                        new File(super.packInfo.getHomeDir().getAbsolutePath() + File.separator + imageTar));
            }
            Logger.info("导出 Docker 镜像包 " + imageTar + " 成功.");
        } catch (Exception  e) {
            throw new DockerPackException(ExceptionEnum.DOCKER_SAVE_EXCEPTION.getMsg(), e);
        }
    }

    /**
     * 推送像 Docker 镜像到远程仓库.
     */
    private void pushImage() {
        // 构建 Registry 授权对象实例，并做校验.
        Docker dockerInfo = super.packInfo.getDocker();
        final String registry = dockerInfo == null ? "" : dockerInfo.getRegistry();
        Logger.info("正在校验推送镜像时需要的 registry 授权是否合法...");

        try {
            RegistryAuth auth = StringUtils.isBlank(registry)
                    ? RegistryAuth.fromDockerConfig().build()
                    : RegistryAuth.fromDockerConfig(registry).build();

            int statusCode = dockerClient.auth(auth);
            if (statusCode != 200) {
                Logger.warn("校验 registry 授权不通过，不能推送镜像到远程镜像仓库中.");
                return;
            }

            // 推送镜像到远程镜像仓库中.
            Logger.info("正在推送 " + this.imageName + " 镜像到远程仓库中...");
            dockerClient.push(StringUtils.isBlank(registry) ? this.imageName : registry + "/" + this.imageName,
                    this::printProgress, auth);
        } catch (Exception e) {
            throw new DockerPackException(ExceptionEnum.DOCKER_PUSH_EXCEPTION.getMsg(), e);
        }
        Logger.info("推送 " + this.imageName + " 镜像到远程仓库中成功.");
    }

    /**
     * 做 Docker 构建相关的判断和处理.
     *
     * <p>包括检查 Docker 环境、初始化 Dcokerfile 和复制 jar 包为同一临时目录下，构建镜像、导出镜像、推送镜像等.</p>
     */
    private void doBuild() {
        this.checkDockerEnv();
        this.doBuildWithoutCheck();
    }

    /**
     * 做 Docker 构建相关的判断和处理，该方法不检查 Docker 环境.
     *
     * <p>包括：初始化 Dcokerfile 和复制 jar 包为同一临时目录下，构建镜像、导出镜像、推送镜像等.</p>
     */
    private void doBuildWithoutCheck() {
        this.initDockerfileAndJar();
        this.buildImage();

        // 如果 docker 的配置信息为空，则直接视为指构建镜像.
        String[] goalTypes;
        Docker dockerInfo = super.packInfo.getDocker();
        if (dockerInfo == null || (goalTypes = dockerInfo.getExtraGoals()) == null || goalTypes.length == 0) {
            Logger.debug("在 jpack 中未配置  docker 额外构建目标类型'goalTypes'的值，只会构建镜像.");
            return;
        }

        // 将构建目标的字符串转换为枚举，存入到 set 集合中.
        Set<DockerGoalEnum> goalEnumSet = new HashSet<>(4);
        for (String goal : goalTypes) {
            DockerGoalEnum goalEnum = DockerGoalEnum.of(goal);
            if (goalEnum != null) {
                goalEnumSet.add(goalEnum);
            }
        }

        // 判断配置的目标类型的值是否合法，不合法提示.
        if (goalEnumSet.isEmpty()) {
            Logger.warn("在 jpack 中配置 docker 的额外构建目标类型'goalTypes'的值不是 save 或者 push，将忽略后续的构建.");
            return;
        }

        // 区分目标包含 'save', 'push' 等两种可能混合使用的场景，注意都需要事先 'build' 构建镜像，
        // 如果目标枚举就一个，就需要区分是 save ，还是 push ,分别处理，否则的话，就都处理.
        if (goalEnumSet.size() == 1) {
            if (goalEnumSet.contains(DockerGoalEnum.SAVE)) {
                this.saveImage();
            } else if (goalEnumSet.contains(DockerGoalEnum.PUSH)) {
                this.pushImage();
            }
        } else {
            this.saveImage();
            this.pushImage();
        }
    }

    /**
     * 复制 Dockerfile 文件到docker平台的目录中.
     */
    private void copyDockerfile() throws IOException {
        // 如果未配置 Dockerfile 文件，则默认生成一个简单的 SpringBoot 服务需要的 Dockerfile 文件.
        Docker docker = super.packInfo.getDocker();
        if (docker == null || StringUtils.isBlank(docker.getDockerfile())) {
            Logger.info("你未配置自定义的 Dockerfile 文件，将使用 jpack 默认提供的 Dockerfile 文件来构建 Docker 镜像.");
            TemplateKit.renderFile("docker/" + DOCKER_FILE, super.buildBaseTemplateContextMap(),
                    super.platformPath + File.separator + DOCKER_FILE);
            return;
        }

        Logger.info("开始渲染你自定义的 Dockerfile 文件中的内容.");
        FileUtils.copyFileToDirectory(super.isRootPath(docker.getDockerfile())
                ? DOCKER_FILE : docker.getDockerfile(), super.platformPath);
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
     * 打印完成信息.
     */
    private void printFinished() {
        Logger.info("jpack 关于 Docker 的相关构建操作执行完毕.");
    }

    /**
     * 静默关闭 Docker Client，删除 Docker 文件夹.
     */
    private void clean() {
        if (this.dockerClient != null) {
            this.dockerClient.close();
        }

        try {
            FileUtils.forceDelete(super.platformPath);
        } catch (Exception e) {
            // 这里"静默"删除即可，即时发生异常，也不用打印异常信息.
            Logger.debug("删除清除 docker 下的临时文件失败.");
        }
    }

}
