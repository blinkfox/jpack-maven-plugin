package com.blinkfox.jpack.exception;

/**
 * jpack 打包构建等相关的运行时异常.
 *
 * @author blinkfox on 2020-06-22.
 * @since v1.5.0
 */
public class PackException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 附带 msg 信息的构造方法.
     *
     * @param msg 消息
     */
    public PackException(String msg) {
        super(msg);
    }

    /**
     * 附带 msg 信息的构造方法.
     *
     * @param msg 消息
     * @param t Throwable实例.
     */
    public PackException(String msg, Throwable t) {
        super(msg, t);
    }

}
