package com.github.monsterhxw.rpc.netty.serialize;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public class SerializeException extends RuntimeException {

    public SerializeException(String message) {
        super(message);
    }

    public SerializeException(String message, Throwable cause) {
        super(message, cause);
    }
}
