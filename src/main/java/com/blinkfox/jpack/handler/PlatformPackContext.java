package com.blinkfox.jpack.handler;

import com.blinkfox.jpack.consts.PlatformEnum;
import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.handler.impl.DockerPackHandler;
import com.blinkfox.jpack.handler.impl.LinuxPackHandler;
import com.blinkfox.jpack.handler.impl.WindowsPackHandler;
import com.blinkfox.jpack.utils.Logger;
import com.blinkfox.jpack.utils.TimeKit;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 各平台下打包的上下文处理器类.
 *
 * @author blinkfox on 2019-05-03.
 */
public class PlatformPackContext {

    private static final String START = "\n"
            + "-------------------------- jpack start packing... -------------------------\n"
            + "         __                          __    \n"
            + "        |__|______  _____     ____  |  | __\n"
            + "        |  |\\____ \\ \\__  \\  _/ ___\\ |  |/ /\n"
            + "        |  ||  |_> > / __ \\_\\  \\___ |    < \n"
            + "    /\\__|  ||   __/ (____  / \\___  >|__|_ \\\n"
            + "    \\______||__|         \\/      \\/      \\/\n";

    /**
     * 用来存储各个平台打包的 map.
     */
    private static final Map<PlatformEnum, PackHandler> packMap = new LinkedHashMap<>(4);

    static {
        packMap.put(PlatformEnum.WINDOWS, new WindowsPackHandler());
        packMap.put(PlatformEnum.LINUX, new LinuxPackHandler());
        packMap.put(PlatformEnum.DOCKER, new DockerPackHandler());
    }

    /**
     * 根据打包的相关参数进行打包的方法.
     *
     * @param platforms 需要打包的平台的数组
     * @param packInfo 打包的相关参数实体
     */
    public void pack(String[] platforms, PackInfo packInfo) {
        Logger.info(START);
        final long start = System.nanoTime();

        // 如果各个打包的平台为空，则默认视为所有平台都打包.
        if (ArrayUtils.isEmpty(platforms)) {
            for (PackHandler packHandler : packMap.values()) {
                packHandler.pack(packInfo);
            }
            this.printEndTimeLine(start);
            return;
        }

        // 遍历各个平台的字符串，将其转换为 PlatformEnum 之后，进行打包.
        for (String platform : platforms) {
            if (StringUtils.isNotBlank(platform)) {
                PlatformEnum platformEnum = PlatformEnum.of(platform.trim().toLowerCase());
                if (platformEnum != null) {
                    packMap.get(platformEnum).pack(packInfo);
                }
            }
        }
        this.printEndTimeLine(start);
    }

    /**
     * 打印结束符及打包花费的时间.
     *
     * @param start 开始时间
     */
    private void printEndTimeLine(long start) {
        Logger.info("\n------------- jpack has been packaged to end. [costs: "
                + TimeKit.convertTime(System.nanoTime() - start) + "] -------------\n");
    }

}
