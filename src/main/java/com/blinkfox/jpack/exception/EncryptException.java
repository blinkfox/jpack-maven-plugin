package com.blinkfox.jpack.exception;

/**
 * 加解密的运行时异常.
 *
 * @author blinkfox on 2020-06-03.
 * @since v1.4.0
 */
public class EncryptException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 附带msg信息的构造方法.
     *
     * @param msg 消息
     * @param t Throwable实例.
     */
    public EncryptException(String msg, Throwable t) {
        super(msg, t);
    }

}
