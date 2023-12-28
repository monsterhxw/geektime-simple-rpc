package com.github.monsterhxw.rpc.netty.transport;

import com.github.monsterhxw.rpc.netty.transport.exception.RemotingConnectionException;

import java.net.SocketAddress;
import java.util.concurrent.TimeoutException;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public interface TransportClient {

    Transport createTransport(SocketAddress address, int connectionTimeout) throws InterruptedException, TimeoutException, RemotingConnectionException;

    void stop();
}
