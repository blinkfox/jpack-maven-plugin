package com.blinkfox.jpack.consts;

import com.blinkfox.jpack.entity.BaseConfig;
import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.utils.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * 平台的枚举类.
 *
 * @author blinkfox on 2019-05-01.
 * @since v1.0.0
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PlatformEnum {

    /**
     * windows.
     */
    WINDOWS("windows") {
        @Override
        public PackInfo mergeNewPackInfo(PackInfo packInfo) {
            return newBaseConfigPackInfo(packInfo, packInfo.getWindows());
        }
    },

    /**
     * linux.
     */
    LINUX("linux") {
        @Override
        public PackInfo mergeNewPackInfo(PackInfo packInfo) {
            return newBaseConfigPackInfo(packInfo, packInfo.getLinux());
        }
    },

    /**
     * docker.
     */
    DOCKER("docker") {
        @Override
        public PackInfo mergeNewPackInfo(PackInfo packInfo) {
            return newBaseConfigPackInfo(packInfo, packInfo.getDocker());
        }
    },

    /**
     * helmChart.
     */
    HELM_CHART("helmChart") {
        @Override
        public PackInfo mergeNewPackInfo(PackInfo packInfo) {
            return newBaseConfigPackInfo(packInfo, packInfo.getHelmChart());
        }
    };

    /**
     * 属性值.
     */
    private final String code;

    /**
     * 根据 platform 的字符串值转换为 PlatformEnum 的值.
     *
     * @param platform 平台字符串
     * @return PlatformEnum实例
     */
    public static PlatformEnum of(String platform) {
        for (PlatformEnum platformEnum : PlatformEnum.values()) {
            if (platformEnum.code.equalsIgnoreCase(platform)) {
                return platformEnum;
            }
        }
        return null;
    }

    /**
     * 获取所有平台的List集合.
     *
     * @return 集合
     */
    public static List<PlatformEnum> getPlatformList() {
        List<PlatformEnum> platformList = new ArrayList<>(PlatformEnum.values().length);
        Collections.addAll(platformList, PlatformEnum.values());
        return platformList;
    }

    /**
     * 创建一个新的 PackInfo 实例，且赋予了各个平台自己的一些配置信息数据.
     *
     * @param packInfo PackInfo
     * @param baseConfig baseConfig
     * @return PackInfo 实例
     */
    public static PackInfo newBaseConfigPackInfo(PackInfo packInfo, BaseConfig baseConfig) {
        PackInfo newPackInfo = PackInfo.newCommonPackInfo(packInfo);
        if (baseConfig == null) {
            Logger.debug("【构建打包 -> 构建】构建打包的相关信息为：【" + newPackInfo + "】.");
            return newPackInfo;
        }

        if (StringUtils.isNotBlank(baseConfig.getVmOptions())) {
            newPackInfo.setVmOptions(baseConfig.getVmOptions());
        }
        if (StringUtils.isNotBlank(baseConfig.getProgramArgs())) {
            newPackInfo.setProgramArgs(baseConfig.getProgramArgs());
        }
        if (ArrayUtils.isNotEmpty(baseConfig.getConfigFiles())) {
            newPackInfo.setConfigFiles(ArrayUtils.addAll(packInfo.getConfigFiles(), baseConfig.getConfigFiles()));
        }
        if (ArrayUtils.isNotEmpty(baseConfig.getCopyResources())) {
            newPackInfo.setCopyResources(
                    ArrayUtils.addAll(packInfo.getCopyResources(), baseConfig.getCopyResources()));
        }
        if (ArrayUtils.isNotEmpty(baseConfig.getExcludeFiles())) {
            newPackInfo.setExcludeFiles(ArrayUtils.addAll(packInfo.getExcludeFiles(), baseConfig.getExcludeFiles()));
        }
        Logger.debug("【构建打包 -> 构建】构建打包的相关信息为：【" + newPackInfo + "】.");
        return newPackInfo;
    }

    /**
     * 根据 PackInfo 对象信息合并出适合各个平台自己的一个新的 PackInfo 对象，用于覆盖通用的配置信息.
     * <p>合并策略如下：</p>
     * <ul>
     *     <li>针对配置项只有一个值的情况，使用"覆盖"的方式来合并配置项，各平台自己的配置项优先级最高，为空时使用公用的配置项. </li>
     *     <li>针对配置项有多个值的情况，使用取"并集"的方式来合并配置项，各平台自己的配置项和公用的配置项取并集. </li>
     * </ul>
     *
     * @param packInfo PackInfo对象
     * @return PackInfo对象
     */
    public abstract PackInfo mergeNewPackInfo(PackInfo packInfo);

    /**
     * 获取该平台的 code 值.
     *
     * @return code
     */
    public String getCode() {
        return code;
    }

}
