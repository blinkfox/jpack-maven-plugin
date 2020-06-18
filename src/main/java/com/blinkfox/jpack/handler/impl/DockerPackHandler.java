package com.blinkfox.jpack.handler.impl;

import com.blinkfox.jpack.consts.DockerGoalEnum;
import com.blinkfox.jpack.consts.ExceptionEnum;
import com.blinkfox.jpack.consts.PlatformEnum;
import com.blinkfox.jpack.consts.SkipErrorEnum;
import com.blinkfox.jpack.entity.Docker;
import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.entity.RegistryUser;
import com.blinkfox.jpack.exception.DockerPackException;
import com.blinkfox.jpack.handler.AbstractPackHandler;
import com.blinkfox.jpack.utils.AesKit;
import com.blinkfox.jpack.utils.Logger;
import com.blinkfox.jpack.utils.TemplateKit;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ProgressMessage;
import com.spotify.docker.client.messages.RegistryAuth;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.io.RawInputStreamFacade;

/**
 * Docker 下的构建、打包的处理器实现类.
 *
 * @author blinkfox on 2019-05-09.
 * @since v1.1.0
 */
public class DockerPackHandler extends AbstractPackHandler {

    /**
     * Dockerfile 配置文件的名称常量.
     */
    private static final String DOCKER_FILE = "Dockerfile";

    /**
     * 正确情况下的编码.
     */
    private static final int SUCCESS_CODE = 200;

    /**
     * Docker 客户端.
     */
    private DockerClient dockerClient;

    /**
     * 镜像名称.
     */
    private String imageName;

    /**
     * 是否对镜像打了标签.
     *
     * @since v1.5.0
     */
    private boolean tagged;

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
            Logger.error("【构建失败 -> 失败】jpack 执行 Docker 构建失败。");
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

            // 初始化 ~/.dockercfg 文件，防止进行授权时报文件找不到的异常！
            this.initDockercfgFile();
        } catch (Exception e) {
            throw new DockerPackException(ExceptionEnum.NO_DOCKER.getMsg(), e);
        }
    }

    /**
     * 初始化 Dockerfile 文件和复制出 jar 包.
     */
    private void initDockerfileAndJar() {
        super.createPlatformCommonDir(PlatformEnum.DOCKER);
        this.copyJarToDockerTargetDir();
        try {
            this.copyDockerfile();
        } catch (IOException e) {
            throw new DockerPackException(ExceptionEnum.NO_DOCKERFILE.getMsg(), e);
        }
    }

    /**
     * 复制 jar 包到 target 目录下，方便默认的 Dockerfile 构建.
     */
    private void copyJarToDockerTargetDir() {
        try {
            FileUtils.copyFileToDirectory(packInfo.getTargetDir().getAbsolutePath() + File.separator
                    + packInfo.getFullJarName(), this.getJpackTargetDir());
        } catch (IOException e) {
            throw new DockerPackException(ExceptionEnum.COPY_JAR_TO_TARGET_EXCEPTION.getMsg(), e);
        }
    }

    /**
     * 构建该服务的 Docker 镜像.
     */
    private void buildImage() {
        try {
            this.imageName = super.packInfo.getDocker().getImageName();
            Logger.info("【构建镜像 -> 进行】正在构建【" + this.imageName + "】镜像...");
            String imageId = dockerClient.build(Paths.get(super.platformPath), imageName, this::printProgress);
            Logger.info("【构建镜像 -> 成功】构建【" + this.imageName + "】镜像完毕，镜像ID: " + imageId);
            FileUtils.deleteDirectory(this.getJpackTargetDir());
        } catch (Exception e) {
            throw new DockerPackException(ExceptionEnum.DOCKER_BUILD_EXCEPTION.getMsg(), e);
        }
    }

    /**
     * 获取 jpack 中的 target 目录.
     *
     * @return target 路径.
     */
    private String getJpackTargetDir() {
        return super.platformPath + File.separator + "target";
    }

    /**
     * 导出该服务的镜像为 '.tar' 包.
     */
    private void saveImage() {
        // 导出之前对镜像进行打标签.
        this.tagImage();

        try {
            String imageTgz = super.packInfo.getDocker().getImageTarName();
            Logger.info("【导出镜像 -> 进行】正在导出 Docker 镜像包: " + imageTgz + " ...");
            // 导出镜像为 `.tgz` 文件.
            try (InputStream imageInput = dockerClient.save(this.imageName)) {
                FileUtils.copyStreamToFile(new RawInputStreamFacade(imageInput),
                        new File(super.platformPath + File.separator + imageTgz));
            }
            Logger.info("【导出镜像 -> 成功】从 Docker 中导出镜像包 " + imageTgz + " 成功.");
            this.handleFilesAndCompress();
        } catch (Exception e) {
            throw new DockerPackException(ExceptionEnum.DOCKER_SAVE_EXCEPTION.getMsg(), e);
        }
    }

    /**
     * 将需要打包的相关文件压缩成 tar.gz 格式的压缩包.
     *
     * <p>需要生成 docs 目录，复制默认的 README.md，将这些相关文件压缩成 .tar.gz 压缩包.</p>
     * <p>文件包括：镜像包 xxx.tar, docs, README.md 等.</p>
     */
    private void handleFilesAndCompress() throws IOException {
        FileUtils.forceMkdir(new File(super.platformPath + File.separator + "docs"));
        super.copyFiles("docker/README.md", "README.md");
        super.compress(PlatformEnum.DOCKER);
    }

    /**
     * 给镜像打含`registry`前缀的标签，便于后续的镜像推送.
     *
     * @return 打了含`registry`前缀的标签
     */
    private String tagImage() {
        // 如果 registry 为空，则不需要打标签，直接返回镜像名称即可.
        String registry = super.packInfo.getDocker().getRegistry();
        if (StringUtils.isBlank(registry)) {
            return this.imageName;
        }

        // 判断是否已经打过标签了，如果已经打过标签就直接返回镜像标签名称即可.
        String imageTagName = registry + "/" + this.imageName;
        if (this.tagged) {
            return imageTagName;
        }

        try {
            dockerClient.tag(this.imageName, imageTagName, true);
            this.tagged = true;
            Logger.info("【镜像标签 -> 成功】已对本次构建的镜像打了标签，标签为：【" + imageTagName + "】.");
            return imageTagName;
        } catch (Exception e) {
            throw new DockerPackException(ExceptionEnum.DOCKER_TAG_EXCEPTION.getMsg(), e);
        }
    }

    /**
     * 推送像 Docker 镜像到远程仓库.
     */
    private void pushImage() {
        // 校验推送的授权是否合法，不合法就不能推送.
        Pair<RegistryAuth, Integer> authPair = this.validRegistryAuth();
        if (authPair.getRight() != SUCCESS_CODE) {
            Logger.warn("【权限认证 -> 失败】校验 registry 授权不通过，不能推送镜像到远程镜像仓库中.");
            return;
        }

        try {
            // 判断 registry 是否配置，如果没有配置就认为默认推送到 dockerhub,就不需要打标签，
            // 否则就需要打含 `registry` 前缀的标签.
            final String imageTagName = this.tagImage();

            // 推送镜像到远程镜像仓库中.
            Logger.info("【推送镜像 -> 进行】正在推送标签为【" + imageTagName + "】的镜像到远程仓库中...");
            dockerClient.push(imageTagName, this::printProgress, authPair.getLeft());
            Logger.info("【推送镜像 -> 成功】推送标签为【" + imageTagName + "】的镜像到远程仓库完成.");
        } catch (Exception e) {
            throw new DockerPackException(ExceptionEnum.DOCKER_PUSH_EXCEPTION.getMsg(), e);
        }
    }

    /**
     * 校验 registry 授权是否合法.
     *
     * @return RegistryAuth 和校验结果
     */
    private Pair<RegistryAuth, Integer> validRegistryAuth() {
        // 构建 Registry 授权对象实例，并做校验.
        Logger.info("【权限认证 -> 进行】正在校验推送镜像时需要的 registry 授权是否合法...");
        RegistryUser registryUser = super.packInfo.getDocker().getRegistryUser();

        // 从 Docker 环境中获取配置授权信息，并校验.
        RegistryAuth auth = null;
        try {
            String username;
            String password;
            if (registryUser != null
                    && StringUtils.isNotBlank(username = registryUser.getUsername())
                    && StringUtils.isNotBlank(password = registryUser.getPassword())) {
                RegistryAuth.Builder builder = RegistryAuth.builder()
                        .username(AesKit.decrypt(username))
                        .password(AesKit.decrypt(password));

                this.setRegistryAuthServerAddress(builder, registryUser.getServerAddress());
                if (StringUtils.isNotBlank(registryUser.getEmail())) {
                    builder.email(registryUser.getEmail());
                }

                if (StringUtils.isNotBlank(registryUser.getIdentityToken())) {
                    builder.identityToken(registryUser.getIdentityToken());
                }
                auth = builder.build();
            } else {
                auth = RegistryAuth.fromDockerConfig().build();
            }
            return Pair.of(auth, dockerClient.auth(auth));
        } catch (Exception e) {
            Logger.error("【权限认证 -> 出错】获取并校验推送镜像的 registry 授权出错！", e);
            return Pair.of(auth, 0);
        }
    }

    /**
     * 设置推送镜像到远程仓库的服务地址信息.
     *
     * @param builder RegistryAuth 的构建器对象
     * @param serverAddress 配置在 {@code registryUser} 中的远程镜像仓库的服务地址
     * @author blinkfox on 2020-06-03
     * @since v1.4.0
     */
    private void setRegistryAuthServerAddress(RegistryAuth.Builder builder, String serverAddress) {
        if (StringUtils.isNotBlank(serverAddress)) {
            builder.serverAddress(serverAddress);
            return;
        }

        // 如果 serverAddress 中未配置服务地址信息，就使用 registry 中的信息.
        String registry = super.packInfo.getDocker().getRegistry();
        if (StringUtils.isBlank(registry)) {
            Logger.warn("【推送镜像 -> 缺失】检测到推送镜像时，你未配置【registry】的服务地址信息.");
            return;
        }
        builder.serverAddress(registry);
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

        // 如果 docker 的配置信息为空，则直接视为只构建镜像.
        String[] goalTypes;
        Docker dockerInfo = super.packInfo.getDocker();
        if (dockerInfo == null || (goalTypes = dockerInfo.getExtraGoals()) == null || goalTypes.length == 0) {
            Logger.debug("【构建目标 -> 镜像】在 jpack 中未配置 docker 额外构建目标类型'goalTypes'的值，只会构建镜像.");
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
            Logger.warn("【构建目标 -> 无效】在 jpack 中配置 docker 的额外构建目标类型'goalTypes'的值不是 save 或者 push，将忽略后续的构建.");
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
        // 如果未配置 Dockerfile 文件，则默认生成一个简单的 SpringBoot 服务需要的 Dockerfile 文件，用于构建镜像.
        Docker docker = super.packInfo.getDocker();
        if (StringUtils.isBlank(docker.getDockerfile())) {
            Logger.info("【构建镜像 -> 默认】将使用 jpack 默认提供的 Dockerfile 文件来构建镜像.");
            Map<String, Object> context = super.buildBaseTemplateContextMap();
            context.put("jdkImage", docker.getFromImage());
            context.put("valume", this.buildVolumes(docker.getVolumes()));
            context.put("customCommands", this.buildCustomCommands(docker.getCustomCommands()));
            context.put("expose", this.buildExpose(docker.getExpose()));
            TemplateKit.renderFile("docker/" + DOCKER_FILE, context,
                    super.platformPath + File.separator + DOCKER_FILE);
            return;
        }

        // 判断配置的 Dockerfile 文件是否有效.
        Logger.info("【构建镜像 -> 渲染】开始渲染你自定义的 Dockerfile 文件中的内容.");
        String dockerFilePath = docker.getDockerfile();
        File dockerFile = new File(super.isRootPath(dockerFilePath) ? DOCKER_FILE : dockerFilePath);
        if (!dockerFile.exists() || dockerFile.isDirectory()) {
            throw new DockerPackException(ExceptionEnum.NO_DOCKERFILE.getMsg());
        }

        FileUtils.copyFileToDirectory(dockerFile, new File(super.platformPath));
    }

    /**
     * 根据 volumes 数组拼接 VOLUME 的字符串.
     * <p>如果输入为：`{"/tmp", "/logs"}` 数组，则输出的是`VOLUME ["/temp", "/logs"]` 字符串.</p>
     *
     * @param volumes volumes 数组
     * @return VOLUME 的字符串
     */
    private String buildVolumes(String[] volumes) {
        return ArrayUtils.isNotEmpty(volumes)
                ? "VOLUME [\"" + StringUtils.join(volumes, "\", \"") + "\"]\n" : "";
    }

    /**
     * 根据要暴露的端口 expose 的值来拼接 EXPOSE 的字符串.
     *
     * @param expose 暴露的端口
     * @return EXPOSE 的字符串
     */
    private String buildExpose(String expose) {
        return StringUtils.isEmpty(expose) ? "" : "\nEXPOSE " + expose.trim() + "\n";
    }

    /**
     * 根据自定义命令数组 customCommands 来拼接 Dockerfile 文件中的各种命令字符串，一条命令就独占一行.
     *
     * @param customCommands 自定义命令数组
     * @return 多条命令的字符串
     */
    private String buildCustomCommands(String[] customCommands) {
        StringBuilder sb = new StringBuilder();
        if (ArrayUtils.isNotEmpty(customCommands)) {
            // 每条命令独占一行.
            for (String command : customCommands) {
                sb.append(command).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 在当前操作系统的用户目录下初始化创建一个 `.dockercfg` 文件，如果没有就初始化一个空文件，否则就不管.
     * <p>注意：这个操作的目的是防止校验授权时报错.</p>
     */
    private void initDockercfgFile() {
        // 初始化创建可能用得到的配置文件.
        String parentPath = System.getProperty("user.home") + File.separator;
        String dockerCfg = parentPath + ".dockercfg";
        String configJson = parentPath + ".docker" + File.separator + "config.json";
        this.initFilesIfNotExists(dockerCfg, configJson);

        // 如果 config.json 文件中的内容是空的，就随便写入一条 JSON 数据，以防止报错.
        File configFile = new File(configJson);
        try {
            String content = org.apache.commons.io.FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
            if (StringUtils.isBlank(content)) {
                org.apache.commons.io.FileUtils.writeByteArrayToFile(configFile,
                        "{\"credsStore\":\"wincred\"}".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            Logger.error("【读取配置 -> 失败】读取或写入文件内容到 ~/.docker/config.json 中出错！", e);
        }
    }

    /**
     * 初始化创建一些可能需要的不存在的文件文件.
     *
     * @param paths 多个文件路径
     */
    private void initFilesIfNotExists(String... paths) {
        for (String path : paths) {
            File file = new File(path);
            if (!file.exists()) {
                try {
                    org.apache.commons.io.FileUtils.touch(file);
                } catch (IOException e) {
                    Logger.error("【创建文件 -> 出错】初始化创建【" + path + "】文件出错！", e);
                }
            }
        }
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
        Logger.debug("【构建结果 -> 完毕】jpack 关于 Docker 的相关构建操作执行完毕.");
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
            Logger.debug("【删除文件 -> 失败】删除清除 docker 下的临时文件失败.");
        }
    }

}
