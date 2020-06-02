package com.blinkfox.jpack.handler;

import com.blinkfox.jpack.consts.PlatformEnum;
import com.blinkfox.jpack.entity.PackInfo;
import com.blinkfox.jpack.handler.impl.DockerPackHandler;
import com.blinkfox.jpack.handler.impl.LinuxPackHandler;
import com.blinkfox.jpack.handler.impl.WindowsPackHandler;
import com.blinkfox.jpack.utils.Logger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 各平台下打包的上下文处理器类.
 *
 * @author blinkfox on 2019-05-03.
 * @since v1.0.0
 */
public class PlatformPackContext {

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
        // 如果各个打包的平台不为空，就遍历取得其对应的枚举值，否则就默认视为所有平台都打包.
        List<PlatformEnum> platformEnums = this.convertPlatformList(platforms);

        // 初始化线程池和 CountDownLatch 的对象实例.
        int count = platformEnums.size();
        CountDownLatch countDownLatch = new CountDownLatch(count);
        ExecutorService executorService = Executors.newFixedThreadPool(count, r -> new Thread(r, "jpack-thread"));

        // 遍历各个平台，多线程进行构建和打包.
        for (PlatformEnum platformEnum : platformEnums) {
            executorService.submit(() -> {
                try {
                    packMap.get(platformEnum).pack(platformEnum.mergeNewPackInfo(packInfo));
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        // 等待所有线程执行完毕，并关闭线程池.
        this.awaitAndShutdown(countDownLatch, executorService);
    }

    /**
     * 转换配置的 platforms 平台为枚举集合.
     *
     * @param platforms 所有 platforms 的数组
     * @return 枚举集合
     */
    private List<PlatformEnum> convertPlatformList(String[] platforms) {
        // 如果各个打包的平台为空，则默认视为对所有平台都打包.
        if (ArrayUtils.isEmpty(platforms)) {
            return PlatformEnum.getPlatformList();
        }

        // 遍历各平台字符串，转换为对应的有效的枚举值.
        List<PlatformEnum> platformEnumList = new ArrayList<>(platforms.length);
        for (String platform : platforms) {
            if (StringUtils.isBlank(platform)) {
                Logger.warn("你配置的 jpack 平台为空！");
                continue;
            }

            // 将 platform 的字符串值转换为平台的枚举值，并添加到集合中.
            PlatformEnum platformEnum = PlatformEnum.of(platform.trim().toLowerCase());
            if (platformEnum == null) {
                Logger.warn("你配置的 jpack 平台 " + platform + " 不存在！");
            } else {
                platformEnumList.add(platformEnum);
            }
        }
        return platformEnumList;
    }

    /**
     * 等待所有线程执行完毕，并最终关闭线程池.
     *
     * @param countDownLatch countDownLatch实例
     * @param executorService 线程池
     */
    private void awaitAndShutdown(CountDownLatch countDownLatch, ExecutorService executorService) {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Logger.error("在多线程下等待 jpack 构建打包结束时出错!", e);
            Thread.currentThread().interrupt();
        } finally {
            executorService.shutdown();
        }
    }

}
