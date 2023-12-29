package com.github.monsterhxw.rpc.netty.transport.netty;

import com.github.monsterhxw.rpc.netty.transport.InFlightRequestManager;
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

    private final InFlightRequestManager inFlightRequestManager = new InFlightRequestManager();

    @Override
    public Transport createTransport(SocketAddress address, int connectionTimeoutMillis, int timeoutMillis) throws InterruptedException, TimeoutException, RemotingConnectionException {
        return TRANSPORT_MAP.computeIfAbsent(address.toString(),
                __ -> new NettyTransport(address, connectionTimeoutMillis, timeoutMillis, inFlightRequestManager));
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

        try {
            inFlightRequestManager.close();
        } catch (IOException ignore) {
            log.warn("Failed to close inFlightRequestManager.", ignore);
        }
    }
}
