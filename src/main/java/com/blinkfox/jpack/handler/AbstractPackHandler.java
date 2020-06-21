package com.blinkfox.jpack.handler;

import com.blinkfox.jpack.consts.PlatformEnum;
import com.blinkfox.jpack.entity.CopyResource;
import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.utils.CompressKit;
import com.blinkfox.jpack.utils.Logger;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.resource.ResourceManager;
import org.codehaus.plexus.resource.loader.FileResourceCreationException;
import org.codehaus.plexus.resource.loader.ResourceNotFoundException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * 各个平台可公用的、抽象的打包处理器抽象类.
 *
 * @author blinkfox on 2019-04-29.
 * @since v1.0.0
 */
public abstract class AbstractPackHandler implements PackHandler {

    private static final String HTTP = "http://";

    private static final String HTTPS = "https://";

    /**
     * bin 主目录的名称常量.
     */
    protected static final String BIN_DIR_NAME = "bin";

    /**
     * 资源管理器对象.
     */
    private ResourceManager resourceManager;

    /**
     * 打包的相关参数实体对象.
     */
    protected PackInfo packInfo;

    /**
     * 各平台中的主目录路径.
     */
    protected String platformPath;

    /**
     * 各平台中的主目录中的 bin 目录路径.
     */
    protected String binPath;

    /**
     * 创建各个平台下的主目录和主目录中的 bin, docs, logs 等目录文件夹.
     *
     * @param platformEnum 平台
     */
    protected void createPlatformCommonDir(PlatformEnum platformEnum) {
        this.initPlatformPath(platformEnum);

        try {
            // 初始化资源管理器对象，用于获取 resources 下的资源.
            this.resourceManager = (ResourceManager) new DefaultPlexusContainer().lookup(ResourceManager.ROLE);

            // 创建或清空各平台的主目录.
            FileUtils.mkdir(this.platformPath);

            // 复制 target 目录中的 jar 包到各平台目录中，复制 application.yml 等文件到 config 目录中.
            this.copyJarAndConfigFile();
        } catch (PlexusContainerException | ComponentLookupException e) {
            Logger.error("【生成文件 -> 出错】创建【" + this.platformPath + "】目录或者复制相关的资源失败！请检查文件是否正在使用!", e);
        }
    }

    /**
     * 创建基础目录，如：bin, config, docs, logs等.
     */
    protected void createBaseDirs() {
        this.binPath = this.platformPath + File.separator + AbstractPackHandler.BIN_DIR_NAME + File.separator;
        try {
            FileUtils.forceMkdir(new File(binPath));
            FileUtils.forceMkdir(new File(this.platformPath + File.separator + "config"));
            FileUtils.forceMkdir(new File(this.platformPath + File.separator + "docs"));
            FileUtils.forceMkdir(new File(this.platformPath + File.separator + "logs"));
        } catch (IOException e) {
            Logger.error("【生成文件 -> 出错】创建【" + this.platformPath + "】目录下的 bin、config、docs、logs 等"
                    + "目录出错！请检查文件是否正在使用!", e);
        }
    }

    /**
     * 分别复制 jar 包和 application.yml 等配置文件到平台目录和 config 目录中.
     */
    private void copyJarAndConfigFile() {
        String jar = packInfo.getFullJarName();
        try {
            // 如果平台不是 Helm Chart 就打包 jar 文件到各平台中.
            if (!this.platformPath.endsWith(PlatformEnum.HELM_CHART.getCode())) {
                FileUtils.copyFileToDirectory(packInfo.getTargetDir().getAbsolutePath() + File.separator + jar,
                        platformPath);
            }

            // 复制配置的 configFiles 配置.
            String[] configFiles = this.packInfo.getConfigFiles();
            if (ArrayUtils.isNotEmpty(configFiles)) {
                for (String configFile : configFiles) {
                    this.copyCustomResources(configFile, "config");
                }
            }
        } catch (IOException e) {
            Logger.error("【复制文件 -> 失败】复制【" + jar + "】或 config 文件到【" + platformPath + "】目录中失败！"
                    + "应该还没有打包此文件，异常信息为：" + e.getMessage());
        }
    }

    /**
     * 初始化各平台的基础路径.
     *
     * @param platformEnum 平台枚举类
     */
    private void initPlatformPath(PlatformEnum platformEnum) {
        this.platformPath = packInfo.getHomeDir().getAbsolutePath() + File.separator + platformEnum.getCode();
    }

    /**
     * 复制基础文件到各平台的主目录中，如：`README.md`.
     *
     * @param source 源地址
     * @param destination 目标地址
     */
    protected void copyFiles(String source, String destination) {
        try {
            FileUtils.copyFile(this.resourceManager.getResourceAsFile(source),
                    new File(this.platformPath, destination));
        } catch (IOException | ResourceNotFoundException | FileResourceCreationException e) {
            Logger.error("【复制文件 -> 出错】复制默认资源到平台中出错！", e);
        }
    }

    /**
     * 构建基础的渲染模板引擎的上下文参数的 Map.
     *
     * @return map
     */
    protected Map<String, Object> buildBaseTemplateContextMap() {
        Map<String, Object> context = new HashMap<>(8);
        context.put("name", packInfo.getName());
        context.put("jarName", packInfo.getFullJarName());
        context.put("vmOptions", StringUtils.isBlank(packInfo.getVmOptions()) ? "" : packInfo.getVmOptions());
        context.put("programArgs", StringUtils.isBlank(packInfo.getProgramArgs()) ? "" : packInfo.getProgramArgs());
        return context;
    }

    /**
     * 判断给定的文件路径是否是项目的根路径.
     *
     * @param filePath 文件路径.
     * @return 布尔值
     */
    protected boolean isRootPath(String filePath) {
        return StringUtils.isBlank(filePath) || ".".equals(filePath) || "/".equals(filePath);
    }

    /**
     * 复制配置的自定义资源到各平台的文件夹中.
     */
    private void copyCustomResources() {
        CopyResource[] copyResources = packInfo.getCopyResources();
        if (ArrayUtils.isEmpty(copyResources)) {
            return;
        }

        // 遍历复制资源.
        for (CopyResource copyResource : copyResources) {
            String fromPath = copyResource.getFrom();
            try {
                this.copyCustomResources(fromPath, copyResource.getTo());
            } catch (IOException e) {
                Logger.error("【复制文件 -> 出错】复制配置的自定义资源【" + fromPath + "】到各平台的包中出错！", e);
            }
        }
    }

    /**
     * 需要复制的资源路径.
     *
     * @param from 待复制的资源路径
     * @param to 复制到目的地的目录路径
     * @throws IOException IO异常
     */
    private void copyCustomResources(String from, String to) throws IOException {
        if (StringUtils.isBlank(from)) {
            return;
        }

        // 复制网络url资源到目录中.
        if (from.startsWith(HTTP) || from.startsWith(HTTPS)) {
            String[] arr = from.split("/");
            File dir = new File(this.platformPath + File.separator + to);
            FileUtils.forceMkdir(dir);
            FileUtils.copyURLToFile(new URL(from), new File(dir + File.separator + arr[arr.length - 1]));
        } else {
            // 不是网络资源，则代表是相对路径或绝对路径的资源，
            // 如果源文件不存在，则直接返回
            File sourceFile = new File(from);
            if (!sourceFile.exists()) {
                Logger.warn("【复制资源 -> 无效】需要复制的源资源文件【" + from + "】不存在，请检查！");
                return;
            }

            // 如果 to 是空的或者 `.`、'/', 则表示复制到各平台包的根目录中，否则复制到对应的目录中即可.
            File toDir = new File(this.isRootPath(to) ? this.platformPath : this.platformPath + File.separator + to);

            // 如果需要复制的资源是目录，则直接复制该目录及其下的子目录到目标目录中.
            if (sourceFile.isDirectory()) {
                FileUtils.copyDirectoryStructure(sourceFile, toDir);
            } else {
                FileUtils.copyFileToDirectory(sourceFile, toDir);
            }
        }
    }

    /**
     * 排除（即删除）不需要的文件或目录.
     */
    private void excludeFiles() {
        String[] excludeFiles = packInfo.getExcludeFiles();
        if (ArrayUtils.isEmpty(excludeFiles)) {
            return;
        }

        // 遍历需要删除的资源进行删除.
        for (String path : excludeFiles) {
            String filePath = this.platformPath + File.separator + path;
            File file = new File(filePath);
            if (!file.exists()) {
                Logger.warn("【排除资源 -> 无效】你配置的需要排除的资源【" + filePath + "】不存在，请检查！");
                continue;
            }

            try {
                FileUtils.forceDelete(file);
            } catch (IOException e) {
                Logger.error("【排除资源 -> 出错】删除配置的需要排除的资源【" + filePath + "】出错，出错原因：【"
                        + e.getMessage() + "】。");
            }
        }
    }

    /**
     * 在压缩之前需要做的一些处理.
     */
    private void handleBeforeCompress() {
        // 复制自定义资源到包中.
        this.copyCustomResources();

        // 删除需要排除的资源.
        this.excludeFiles();
    }

    /**
     * 制作 linux 下的 tar.gz 压缩包.
     *
     * @param platformEnum 平台
     */
    protected void compress(PlatformEnum platformEnum) {
        // 在压缩各平台文件夾之前，需要做的一些公共处理操作.
        this.handleBeforeCompress();

        String platform = platformEnum.getCode();
        Logger.debug("【构建打包 -> 进行】正在制作 " + platform + " 下的部署压缩包...");
        try {
            // 制作压缩包.
            switch (platformEnum) {
                case WINDOWS:
                    CompressKit.zip(platformPath, packInfo.getPackName() + ".zip");
                    break;
                case LINUX:
                    CompressKit.tarGz(platformPath, packInfo.getPackName() + ".tar.gz");
                    break;
                case DOCKER:
                    CompressKit.tarGz(platformPath, packInfo.getDockerPackName() + ".tar.gz");
                    break;
                case HELM_CHART:
                    CompressKit.tarGz(platformPath, packInfo.getChartSavePackName() + ".tar.gz");
                    break;
                default:
                    break;
            }

            Logger.debug("【清除文件 -> 进行】正在清除 " + platform + " 临时文件....");
            FileUtils.forceDelete(platformPath);
            Logger.debug("【清除文件 -> 成功】已清除 " + platform + " 临时文件.");
        } catch (IOException e) {
            Logger.error("【构建打包 -> 失败】压缩并清除 " + platform + " 下部署的临时文件失败.", e);
        }
        Logger.info("【构建打包 -> 成功】制作 " + platform + " 下的部署压缩包完成.");
    }

}
