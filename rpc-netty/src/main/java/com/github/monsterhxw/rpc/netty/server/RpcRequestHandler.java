package com.github.monsterhxw.rpc.netty.server;

import com.github.monsterhxw.rpc.api.spi.Singleton;
import com.github.monsterhxw.rpc.netty.transport.RequestHandler;
import com.github.monsterhxw.rpc.netty.transport.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
@Singleton
public class RpcRequestHandler implements RequestHandler, ServiceProviderRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(RpcRequestHandler.class);

    private HashMap<String/*service name*/, Object/*service provider*/> serviceProviders = new HashMap<>();

    @Override
    public <T> void addServiceProvider(Class<? extends T> serviceClass, T serviceProvider) {
        serviceProviders.put(serviceClass.getCanonicalName(), serviceProvider);
        LOG.info("Add service:{}, provider: {}.", serviceClass.getCanonicalName(), serviceProvider.getClass().getCanonicalName());
    }

    @Override
    public Command handle(Command requestCommand) {
        return null;
    }

    @Override
    public int type() {
        return 0;
    }
}
