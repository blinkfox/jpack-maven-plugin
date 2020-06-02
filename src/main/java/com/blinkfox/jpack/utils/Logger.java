package com.blinkfox.jpack.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.maven.plugin.logging.Log;

/**
 * 日志工具类.
 *
 * @author blinkfox on 2019-05-01.
 * @since v1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Logger {

    /**
     * maven 的日志对象.
     */
    private static Log log;

    /**
     * 初始化设置日志对象，必须初始化才行，且最好保证只初始化一次.
     *
     * @param mvnLog maven的日志对象
     */
    public static synchronized void initSetLog(Log mvnLog) {
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
     * 打印 warn 日志.
     *
     * @param content 日志内容
     */
    public static void warn(CharSequence content) {
        log.warn(content);
    }

    /**
     * 打印 error 日志信息.
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
