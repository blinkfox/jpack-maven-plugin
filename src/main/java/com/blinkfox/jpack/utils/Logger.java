package com.blinkfox.jpack.utils;

import org.apache.maven.plugin.logging.Log;

/**
 * 日志工具类.
 *
 * @author blinkfox on 2019-05-01.
 */
public final class Logger {

    /**
     * maven 的日志对象.
     */
    private static Log log;

    /**
     * 构造方法.
     */
    private Logger() {}

    /**
     * 设置日志对象.
     *
     * @param mvnLog maven的日志对象
     */
    public static synchronized void setLog(Log mvnLog) {
        log = mvnLog;
    }

    /**
     * 打印 debug 日志.
     *
     * @param content 日志内容
     */
    public static void debug(CharSequence content) {
        log.debug(content);
    }

    /**
     * 打印 info 日志.
     *
     * @param content 日志内容
     */
    public static void info(CharSequence content) {
        log.info(content);
    }

    /**
     * 打印 error 日志.
     *
     * @param content 日志内容
     */
    public static void error(CharSequence content) {
        log.error(content);
    }

    /**
     * 打印 error 日志和 Throwable 堆栈信息.
     *
     * @param content 日志内容
     * @param error Throwable对象
     */
    public static void error(CharSequence content, Throwable error) {
        log.error(content, error);
    }

}
