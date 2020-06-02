package com.blinkfox.jpack.exception;

/**
 * Docker 构建、打包等过程中的异常.
 *
 * @author blinkfox on 2019-05-13.
 * @since v1.1.0
 */
public class DockerPackException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 附带msg信息的构造方法.
     *
     * @param msg 消息
     */
    public DockerPackException(String msg) {
        super(msg);
    }

    /**
     * 附带msg信息的构造方法.
     *
     * @param msg 消息
     * @param t Throwable实例.
     */
    public DockerPackException(String msg, Throwable t) {
        super(msg, t);
    }

}
