package com.github.monsterhxw.rpc.netty.transport.exception;

/**
 * @author huangxuewei
 * @since 2023/12/29
 */
public class RemotingConnectionException extends Exception {

    public RemotingConnectionException(String address) {
        this(address, null);
    }

    public RemotingConnectionException(String address, Throwable cause) {
        super("connect to " + address + " failed", cause);
    }
}
