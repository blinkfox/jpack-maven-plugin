package com.blinkfox.jpack.utils;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * LoggerTest.
 *
 * @author blinkfox on 2019-05-04.
 */
public class LoggerTest {

    /**
     * 初始化日志实例.
     */
    @BeforeClass
    public static void initSetLog() {
        Logger.initSetLog(new SystemStreamLog());
    }

    /**
     * 测试错误日志的情况.
     */
    @Test
    public void error() {
        Logger.error("My test error log.", new RuntimeException());
    }

}
