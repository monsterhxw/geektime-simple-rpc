package com.github.monsterhxw.rpc.netty;

import com.github.monsterhxw.rpc.api.RpcAccessPoint;
import com.github.monsterhxw.rpc.api.spi.ServiceLoadException;
import com.github.monsterhxw.rpc.api.spi.ServiceSupport;
import com.github.monsterhxw.rpc.netty.client.stub.StubFactory;
import com.github.monsterhxw.rpc.netty.server.ServiceProviderRegistry;
import com.github.monsterhxw.rpc.netty.transport.RequestHandlerRegistry;
import com.github.monsterhxw.rpc.netty.transport.Transport;
import com.github.monsterhxw.rpc.netty.transport.TransportClient;
import com.github.monsterhxw.rpc.netty.transport.TransportServer;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * @author huangxuewei
 * @since 2023/12/26
 */
public class NettyRpcAccessPoint implements RpcAccessPoint {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 9999;

    private final String host;
    private final int port;
    private final URI uri;

    private TransportClient client;
    private final ConcurrentHashMap<URI, Transport> TRANSPORT_MAP = new ConcurrentHashMap<>();
    private StubFactory stubFactory;

    private TransportServer server;
    private ServiceProviderRegistry serviceProviderRegistry;

    public NettyRpcAccessPoint() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public NettyRpcAccessPoint(String host, int port) {
        this.host = host;
        this.port = port;
        this.uri = URI.create(String.format("rpc://%s:%d", host, port));
    }

    @Override
    public <T> T getRemoteService(URI uri, Class<T> serviceClass) {
        Transport transport = getAndCreateTransport(uri);
        return createStub(transport, serviceClass);
    }

    @Override
    public URI getServerUri() {
        return uri;
    }

    @Override
    public <T> URI addServiceProvider(T service, Class<T> serviceClass) {
        synchronized (this) {
            initializeServiceProviderRegistry();
            serviceProviderRegistry.addServiceProvider(serviceClass, service);
            return uri;
        }
    }

    @Override
    public Closeable startServer() throws Exception {
        synchronized (this) {
            if (null == server) {
                server = ServiceSupport.load(TransportServer.class);
                server.start(RequestHandlerRegistry.getInstance(), port);
            }
            return () -> {
                if (null != server) {
                    server.stop();
                }
            };
        }
    }

    @Override
    public void close() throws IOException {
        if (null != server) {
            server.stop();
        }
        if (null != client) {
            client.stop();
        }
    }

    private Transport getAndCreateTransport(URI uri) {
        return TRANSPORT_MAP.computeIfAbsent(uri, __ -> createTransport());
    }

    private void initializeClient() throws ServiceLoadException {
        synchronized (this) {
            if (client == null) {
                client = ServiceSupport.load(TransportClient.class);
            }
        }
    }

    private Transport createTransport() {
        try {
            initializeClient();
            return client.createTransport(new InetSocketAddress(host, port), 30_000L);
        } catch (InterruptedException | TimeoutException | ServiceLoadException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T createStub(Transport transport, Class<T> serviceClass) {
        try {
            initializeStubFactory();
            return stubFactory.createStub(transport, serviceClass);
        } catch (ServiceLoadException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeStubFactory() throws ServiceLoadException {
        synchronized (this) {
            if (null == stubFactory) {
                stubFactory = ServiceSupport.load(StubFactory.class);
            }
        }
    }

    private void initializeServiceProviderRegistry() {
        synchronized (this) {
            if (null == serviceProviderRegistry) {
                try {
                    serviceProviderRegistry = ServiceSupport.load(ServiceProviderRegistry.class);
                } catch (ServiceLoadException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
