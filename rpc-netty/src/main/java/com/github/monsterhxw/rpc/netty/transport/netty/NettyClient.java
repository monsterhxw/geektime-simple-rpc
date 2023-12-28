package com.github.monsterhxw.rpc.netty.transport.netty;

import com.github.monsterhxw.rpc.netty.transport.Transport;
import com.github.monsterhxw.rpc.netty.transport.TransportClient;
import com.github.monsterhxw.rpc.netty.transport.exception.RemotingConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * @author huangxuewei
 * @since 2023/12/29
 */
public class NettyClient implements TransportClient {

    private static final Logger log = LoggerFactory.getLogger(NettyClient.class);

    private final ConcurrentHashMap<String, Transport> TRANSPORT_MAP = new ConcurrentHashMap<>();

    @Override
    public Transport createTransport(SocketAddress address, int connectionTimeout) throws InterruptedException, TimeoutException, RemotingConnectionException {
        return TRANSPORT_MAP.computeIfAbsent(address.toString(), __ -> new NettyTransport(address, connectionTimeout));
    }

    @Override
    public void stop() {
        if (!TRANSPORT_MAP.isEmpty()) {
            TRANSPORT_MAP.forEach((address, transport) -> {
                try {
                    transport.close();
                } catch (IOException ignore) {
                    log.warn("Failed to close address: {}, transport.", address);
                }
            });
            TRANSPORT_MAP.clear();
        }
    }
}
