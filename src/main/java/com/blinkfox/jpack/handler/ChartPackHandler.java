package com.blinkfox.jpack.handler;

import com.blinkfox.jpack.entity.HelmChart;
import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.utils.CmdKit;
import com.blinkfox.jpack.utils.Logger;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * ChartPackHandler.
 *
 * @author blinkfox on 2020-06-18.
 * @since v1.5.0
 */
@Getter
public class ChartPackHandler {

    /**
     * 打包的相关信息.
     */
    private final PackInfo packInfo;

    /**
     * 平台主文件目录.
     */
    private String platformPath;

    /**
     * 打包后 chart 包的路径.
     */
    private String chartTgzPath;

    /**
     * 构造方法.
     *
     * @param packInfo 打包的相关信息.
     */
    public ChartPackHandler(PackInfo packInfo) {
        this.packInfo = packInfo;
    }

    /**
     * 对 chart 文件进行打包.
     */
    public void pack() {
        HelmChart helmChart = packInfo.getDocker().getHelmChart();
        if (helmChart == null) {
            Logger.info("【Chart打包 -> 跳过】没有配置【<helmChart>】的相关内容，将跳过 HelmChart 相关的构建.");
            return;
        }

        // 检查 helm 环境.
        if (!this.checkHelmEnv()) {
            Logger.info("【Helm 环境检查 -> 出错】没有检测到【helm】的环境变量，将跳过 Helm Chart 的相关构建."
                    + "请到这里【https://github.com/helm/helm/releases】下载最新版的 helm，并将其设置到 path 环境变量中.");
            return;
        }

        // 执行真正的打包.
        if (this.saveChart(helmChart.getLocation())) {
            Logger.info("【Chart打包 -> 成功】HelmChart 打包成功.");
        }
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

    private boolean saveChart(String chartLocation) {
        try {
            String result = CmdKit.execute("helm package " + chartLocation);
            if (result.contains("Successfully")) {
                this.chartTgzPath = StringUtils.substringAfterLast(result, "saved it to:").trim();
                return true;
            }
        } catch (Exception e) {
            Logger.error("【Helm打包 -> 出错】执行【helm】打包出错，错误原因如下：", e);
        }
        return false;
    }

    private boolean pushChart() {
        return true;
    }

}
