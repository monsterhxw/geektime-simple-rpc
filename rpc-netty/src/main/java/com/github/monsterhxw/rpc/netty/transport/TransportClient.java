package com.github.monsterhxw.rpc.netty.transport;

import java.net.SocketAddress;
import java.util.concurrent.TimeoutException;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public interface TransportClient {

    Transport createTransport(SocketAddress address, long connectionTimeout) throws InterruptedException, TimeoutException;

    void stop();
}
