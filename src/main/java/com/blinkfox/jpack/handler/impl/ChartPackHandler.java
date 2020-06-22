package com.blinkfox.jpack.handler.impl;

import com.blinkfox.jpack.consts.ChartGoalEnum;
import com.blinkfox.jpack.consts.PlatformEnum;
import com.blinkfox.jpack.entity.HelmChart;
import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.entity.RegistryUser;
import com.blinkfox.jpack.exception.PackException;
import com.blinkfox.jpack.handler.AbstractPackHandler;
import com.blinkfox.jpack.utils.AesKit;
import com.blinkfox.jpack.utils.CmdKit;
import com.blinkfox.jpack.utils.Logger;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.io.RawInputStreamFacade;

/**
 * ChartPackHandler.
 *
 * @author blinkfox on 2020-06-18.
 * @since v1.5.0
 */
@Getter
public class ChartPackHandler extends AbstractPackHandler {

    public static final String PUSH_CURL = "curl -u \"%s:%s\" -X POST \"%s\" -H \"accept: application/json\" "
            + "-H \"Content-Type: multipart/form-data\" -F \"chart=@%s;type=application/x-compressed\"";

    private static final String VERSION = "version";

    private static final String SUCCESS = "success";

    private static final String STR_TRUE = "true";

    /**
     * 进行 Helm Chart 构建的相关信息.
     */
    private HelmChart helmChart;

    /**
     * 打包后 chart 包的路径.
     */
    private String chartTgzPath;

    /**
     * 根据打包的相关参数进行打包的方法.
     *
     * @param packInfo 打包的相关参数实体
     */
    @Override
    public void pack(PackInfo packInfo) {
        super.packInfo = packInfo;
        this.helmChart = packInfo.getHelmChart();
        if (this.helmChart == null) {
            Logger.info("【Chart构建 -> 跳过】没有配置【<helmChart>】的相关内容，将跳过 HelmChart 相关的构建.");
            return;
        }

        // 判断是否有构建目标.
        final String[] goals = this.helmChart.getGoals();
        if (ArrayUtils.isEmpty(goals)) {
            Logger.warn("【Chart构建 -> 跳过】没有配置【<helmChart>】的构建目标【<goals>】，将跳过 HelmChart 相关的构建.");
            return;
        }

        // 检查 helm 环境.
        if (!this.checkHelmEnv()) {
            Logger.info("【Helm 环境检查 -> 出错】没有检测到【helm】的环境变量，将跳过 Helm Chart 的相关构建."
                    + "请到这里【https://github.com/helm/helm/releases】下载最新版的 helm，并将其设置到 path 环境变量中.");
            return;
        }

        super.createPlatformCommonDir(PlatformEnum.HELM_CHART);

        // 将构建目标的字符串转换为枚举，存入到 set 集合中.
        this.doBuild(goals);
    }

    private void doBuild(String[] goals) {
        Set<ChartGoalEnum> goalSet = this.buildChartGoalEnum(goals);
        int goalSize = goalSet.size();
        if (goalSize == 1) {
            ChartGoalEnum goalEnum = ChartGoalEnum.of(goals[0]);
            if (goalEnum == null) {
                Logger.warn("【Chart构建 -> 跳过】你配置的【<helmChart.goals>】构建目标的值不是【package】、"
                        + "【push】、【save】三者中的内容，将会跳过 Helm Chart 的相关构建.");
                return;
            }

            if (goalEnum == ChartGoalEnum.PACKAGE) {
                // 如果只有一个打包目标，则只进行打包即可.
                this.packageChart();
            } else if (goalEnum == ChartGoalEnum.PUSH) {
                // 如果目标是推送，则会先打包，打包成功后再推送.
                if (this.packageChart()) {
                    this.pushChart();
                }
            } else if (goalEnum == ChartGoalEnum.SAVE) {
                // 如果目标是导出更大的包，则也会先打包，打包成功后再导出.
                if (this.packageChart()) {
                    this.saveChart();
                }
            }
        } else if (goalSize == 2) {
            if (goalSet.contains(ChartGoalEnum.PACKAGE) && goalSet.contains(ChartGoalEnum.PUSH)) {
                // 如果目标是打包和推送，则会先打包，打包成功后再推送.
                if (this.packageChart()) {
                    this.pushChart();
                }
            } else if (goalSet.contains(ChartGoalEnum.PACKAGE) && goalSet.contains(ChartGoalEnum.SAVE)) {
                // 如果目标是打包和导出更大的包，则也会先打包，打包成功后再导出.
                if (this.packageChart()) {
                    this.saveChart();
                }
            } else if (goalSet.contains(ChartGoalEnum.PUSH) && goalSet.contains(ChartGoalEnum.SAVE)) {
                // 如果目标是推送和导出更大的包，则也会先打包，打包成功后再推送、导出.
                if (this.packageChart()) {
                    this.pushChart();
                    this.saveChart();
                }
            }
        } else {
            // 否则，表示打包、推送以及导出都做.
            if (this.packageChart()) {
                this.pushChart();
                this.saveChart();
            }
        }
    }

    /**
     * 构建 Chart 目标枚举类的集合.
     *
     * @param goals 目标的字符串数组
     * @return 目标的枚举 Set 集合
     */
    private Set<ChartGoalEnum> buildChartGoalEnum(String[] goals) {
        Set<ChartGoalEnum> goalEnumSet = new HashSet<>(4);
        for (String goal : goals) {
            ChartGoalEnum goalEnum = ChartGoalEnum.of(goal);
            if (goalEnum != null) {
                goalEnumSet.add(goalEnum);
            }
        }
        return goalEnumSet;
    }

    /**
     * 检查 Helm 环境是否符合构建的需求，须在操作系统的 path 变量中配置 {@code helm} 环境变量.
     */
    private boolean checkHelmEnv() {
        try {
            return CmdKit.execute(new String[] {"helm", "version"}).contains(VERSION);
        } catch (Exception e) {
            Logger.warn(e.getMessage());
            return false;
        }
    }

    /**
     * 打包 Chart 为 `.tgz` 格式.
     *
     * @return 布尔值结果
     */
    private boolean packageChart() {
        // 判断 helmChart 源 yaml 文件是否存在，或者是否是目录.
        File file = new File(this.helmChart.getLocation());
        if (!file.exists()) {
            Logger.info("【Chart打包 -> 放弃】Helm Chart 中的各源 yaml 文件不存在【"
                    + this.helmChart.getLocation() + "】，请检查修改【helmChart -> location】 的值.");
            return false;
        }

        if (!file.isDirectory()) {
            Logger.info("【Chart打包 -> 放弃】Helm Chart 中的【" + this.helmChart.getLocation()
                    + "】不是一个目录，请检查修改【helmChart -> location】 的值.");
            return false;
        }

        try {
            // 使用 helm 命令来打包.
            String result = CmdKit.execute(new String[] {"helm", "package", file.getAbsolutePath()});
            if (result.toLowerCase().contains(SUCCESS)) {
                Logger.info("【Chart打包 -> 成功】执行【helm】命令打包成功.");
                File tgzFile = new File(StringUtils.substringAfterLast(result, "to:").trim());
                if (!tgzFile.exists()) {
                    throw new PackException("【Chart打包 -> 失败】未找到打包后的 tgz 文件的位置，请检查，打包的结果为：【" + result + "】.");
                }

                // 复制打包后的文件到 jpack 主目录中，便于获取或后续使用.
                this.chartTgzPath = super.packInfo.getHomeDir() + File.separator + tgzFile.getName();
                FileUtils.copyFile(tgzFile, new File(this.chartTgzPath));
                return true;
            }
        } catch (Exception e) {
            Logger.error("【Chart打包 -> 出错】执行【helm】命令打包 Chart 出错，错误原因如下：", e);
        }
        return false;
    }

    /**
     * 推送 Chart 到远程仓库中.
     */
    private void pushChart() {
        RegistryUser registry = this.helmChart.getRegistryUser();
        String charRepoUrl = this.helmChart.getChartRepoUrl();
        if (StringUtils.isBlank(charRepoUrl) || registry == null
                || StringUtils.isBlank(registry.getUsername()) || StringUtils.isBlank(registry.getPassword())) {
            Logger.warn("【Chart推送 -> 跳过】未配置 registryUser 和 charRepoUrl 相关信息，将不会推送 Chart 包.");
            return;
        }

        // 拼接推送 Chart 的 CURL 命令，并执行推送的命令.
//        String cmd = String.format(PUSH_CURL, AesKit.decrypt(registry.getUsername()),
//                AesKit.decrypt(registry.getPassword()), charRepoUrl, this.chartTgzPath);
        try {
            Logger.info("【Chart推送 -> 开始】开始推送 Chart 包到远程 Registry 仓库中 ...");
            if (CmdKit.execute(buildPushUrl(registry, charRepoUrl)).toLowerCase().contains(STR_TRUE)) {
                Logger.info("【Chart推送 -> 成功】推送 Chart 包到远程 Registry 仓库中成功.");
            }
        } catch (Exception e) {
            Logger.error("【Chart推送 -> 出错】执行 CURL 指令推送 Chart 包出错，错误原因如下：", e);
        }
    }

    private String[] buildPushUrl(RegistryUser registry, String charRepoUrl) {
        List<String> cmdList = new ArrayList<>();
        cmdList.add("curl");
        cmdList.add("-u");
        cmdList.add(AesKit.decrypt(registry.getUsername()) + ":" + AesKit.decrypt(registry.getPassword()));
        cmdList.add("-X");
        cmdList.add("POST");
        cmdList.add(charRepoUrl);
        cmdList.add("-H");
        cmdList.add("accept: application/json");
        cmdList.add("-H");
        cmdList.add("Content-Type: multipart/form-data");
        cmdList.add("-F");
        cmdList.add("chart=@" + this.chartTgzPath + ";type=application/x-compressed");
        return cmdList.toArray(new String[] {});
    }

    /**
     * 将 Chart 包和离线的 Docker 镜像包、copyResource 等相关资源再一起导出成一个更大的发布包.
     */
    private void saveChart() {
        // 创建用来存放镜像和 Chart 文件包的文件夹.
        String imageChartPath = this.platformPath + File.separator + this.packInfo.getName() + File.separator;
        try {
            FileUtils.forceMkdir(new File(imageChartPath));
        } catch (IOException e) {
            throw new PackException("【Chart导出镜像 -> 异常】初始化用来存放镜像和 Chart 文件包的文件夹【" + imageChartPath + "】异常.", e);
        }

        // 开始生成需要导出镜像的名称.
        String[] saveImages = this.helmChart.getSaveImages();
        if (ArrayUtils.isEmpty(saveImages)) {
            saveImages = new String[] {this.packInfo.getDocker().getImageTagName()};
        }

        // 构建导出运行 Chart 所需的镜像.
        Logger.info("【Chart导出镜像 -> 开始】开始从 Docker 中导出 Chart 所需的镜像包 ...");
        try (DockerClient dockerClient = DefaultDockerClient.fromEnv().build();) {
            dockerClient.ping();
            try (InputStream imageInput = dockerClient.save(saveImages)) {
                String saveImageFileName = this.helmChart.getSaveImageFileName();
                saveImageFileName = StringUtils.isBlank(saveImageFileName)
                        ? imageChartPath + "images.tgz"
                        : imageChartPath + saveImageFileName;
                FileUtils.copyStreamToFile(new RawInputStreamFacade(imageInput), new File(saveImageFileName));
                Logger.info("【Chart导出镜像 -> 成功】从 Docker 中导出镜像包 " + saveImageFileName + " 成功.");
            }
        } catch (DockerException | DockerCertificateException e) {
            Logger.error("【Chart导出镜像 -> 放弃】未检测到或开启 Docker 环境，将跳过 Helm Chart 导出时的镜像导出环节.", e);
        } catch (IOException e) {
            Logger.error("【Chart导出镜像 -> 失败】从 Docker 中导出镜像失败.", e);
        } catch (InterruptedException e) {
            Logger.error("【Chart导出镜像 -> 中断】从 Docker 中导出镜像被中断.", e);
            Thread.currentThread().interrupt();
        }

        // 将 chart 源文件或其他文件复制到目标文件夹中.
        File sourceChartFile = new File(chartTgzPath);
        String targetChartPath = imageChartPath + sourceChartFile.getName();
        try {
            FileUtils.copyFile(sourceChartFile, new File(targetChartPath));
            this.handleFilesAndCompress();
        } catch (IOException e) {
            throw new PackException("【Chart导出镜像 -> 异常】复制 Chart 源文件【" + sourceChartFile.getAbsolutePath()
                    + "】到目标文件【" + targetChartPath + "】出错.", e);
        }
    }

    /**
     * 将需要打包的相关文件压缩成 tar.gz 格式的压缩包.
     *
     * <p>需要生成 docs 目录，复制默认的 README.md，将这些相关文件压缩成 .tar.gz 压缩包.</p>
     * <p>文件包括：name/镜像包 xxx.tgz, name/chart.tgz, docs, README.md 等.</p>
     */
    private void handleFilesAndCompress() throws IOException {
        FileUtils.forceMkdir(new File(super.platformPath + File.separator + "docs"));
        super.copyFiles("helmChart/README.md", "README.md");
        super.compress(PlatformEnum.HELM_CHART);
    }

}
