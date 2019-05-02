package com.blinkfox.jpack.exception;

/**
 * PackException.
 *
 * @author blinkfox on 2019-05-02.
 */
public class PackException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 附带msg信息的构造方法.
     *
     * @param msg 消息
     */
    public PackException(String msg) {
        super(msg);
    }

    /**
     * 附带msg信息和 Throwable 实例的构造方法.
     *
     * @param msg 消息
     * @param t Throwable实例.
     */
    public PackException(String msg, Throwable t) {
        super(msg, t);
    }

}
