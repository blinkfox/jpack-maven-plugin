package com.blinkfox.jpack.handler.impl;

import com.blinkfox.jpack.consts.ChartGoalEnum;
import com.blinkfox.jpack.entity.HelmChart;
import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.entity.RegistryUser;
import com.blinkfox.jpack.handler.AbstractPackHandler;
import com.blinkfox.jpack.utils.AesKit;
import com.blinkfox.jpack.utils.CmdKit;
import com.blinkfox.jpack.utils.Logger;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

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
            String result = CmdKit.execute("helm version");
            if (result.contains("version.BuildInfo")) {
                return true;
            }
        } catch (Exception ignore) {
            // ignore.
        }
        return false;
    }

    /**
     * 打包 Chart 为 `.tgz` 格式.
     *
     * @return 布尔值结果
     */
    private boolean packageChart() {
        try {
            String result = CmdKit.execute("helm package " + this.helmChart.getLocation());
            if (result.contains("Successfully")) {
                Logger.info("【Chart打包 -> 成功】执行【helm】命令打包成功.");
                this.chartTgzPath = StringUtils.substringAfterLast(result, "saved it to:").trim();
                return true;
            }
        } catch (Exception e) {
            Logger.error("【Chart打包 -> 出错】执行【helm】命令打包 Chart 出错，错误原因如下：", e);
        }
        return false;
    }

    /**
     * 推送 Chart 到远程仓库中.
     *
     * @return 布尔值结果
     */
    private boolean pushChart() {
        RegistryUser registryUser = this.packInfo.getDocker().getRegistryUser();
        String charRepoUrl = this.helmChart.getChartRepoUrl();
        if (registryUser == null || StringUtils.isBlank(charRepoUrl)) {
            Logger.warn("【Chart推送 -> 跳过】未配置 registryUser 和 charRepoUrl 相关信息，将不会推送 Chart 包.");
            return false;
        }

        // 拼接推送 Chart 的 CURL 命令，并执行推送的命令.
        String cmd = String.format(PUSH_CURL, AesKit.decrypt(registryUser.getUsername()),
                AesKit.decrypt(registryUser.getPassword()), charRepoUrl, this.chartTgzPath);
        try {
            Logger.info("【Chart推送 -> 开始】开始推送 Chart 包到远程 Registry 仓库中 ...");
            String result = CmdKit.execute(cmd);
            if (result.contains("true")) {
                Logger.info("【Chart推送 -> 成功】推送 Chart 包到远程 Registry 仓库中成功.");
                return true;
            }
        } catch (Exception e) {
            Logger.error("【Chart推送 -> 出错】执行 CURL 指令推送 Chart 包出错，错误原因如下：", e);
        }
        return false;
    }

    /**
     * 将 Chart 包和离线的 Docker 镜像包、copyResource 等相关资源再一起导出成一个更大的发布包，
     */
    private void saveChart() {
        // TODO
    }

}
