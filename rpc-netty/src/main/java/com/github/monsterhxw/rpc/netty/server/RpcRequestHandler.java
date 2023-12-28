package com.github.monsterhxw.rpc.netty.server;

import com.github.monsterhxw.rpc.api.spi.Singleton;
import com.github.monsterhxw.rpc.netty.client.ServiceTypes;
import com.github.monsterhxw.rpc.netty.client.stub.RpcRequest;
import com.github.monsterhxw.rpc.netty.serialize.SerializeSupport;
import com.github.monsterhxw.rpc.netty.transport.RequestHandler;
import com.github.monsterhxw.rpc.netty.transport.command.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
@Singleton
public class RpcRequestHandler implements RequestHandler, ServiceProviderRegistry {

    private static final Logger log = LoggerFactory.getLogger(RpcRequestHandler.class);

    private HashMap<String/*service name*/, Object/*service provider*/> serviceProviders = new HashMap<>();

    @Override
    public <T> void addServiceProvider(Class<? extends T> serviceClass, T serviceProvider) {
        serviceProviders.put(serviceClass.getCanonicalName(), serviceProvider);
        log.info("Add service:{}, provider: {}.", serviceClass.getCanonicalName(), serviceProvider.getClass().getCanonicalName());
    }

    @Override
    public Command handle(Command requestCommand) {
        Header header = requestCommand.getHeader();
        // deserialize payload
        RpcRequest rpcRequest = SerializeSupport.deserialize(requestCommand.getPayload());
        Object serviceProvider = serviceProviders.get(rpcRequest.getInterfaceName());

        if (serviceProvider == null) {
            log.warn("No provider: {}, requestId: {}, version: {}, type: {}.", rpcRequest.getInterfaceName(), header.getRequestId(), header.getVersion(), header.getType());
            return CommandSupport.errorRespCommand(header, Code.UNSUPPORTED_TYPE, "No provider: " + rpcRequest.getInterfaceName());
        }

        String args = SerializeSupport.deserialize(rpcRequest.getSerializedArguments());

        try {
            Method method = serviceProvider.getClass().getMethod(rpcRequest.getMethodName(), String.class);
            String result = (String) method.invoke(serviceProvider, args);
            byte[] payload = SerializeSupport.serialize(result);
            return CommandSupport.successRespCommand(header, payload);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Error when invoke method: {}, requestId: {}, version: {}, type: {}, error: {}.", rpcRequest.getMethodName(), header.getRequestId(), header.getVersion(), header.getType(), e.getMessage());
            return CommandSupport.errorRespCommand(header, Code.UNKNOWN_ERROR, e.getMessage());
        }
    }

    @Override
    public int type() {
        return ServiceTypes.TYPE_RPC_REQUEST;
    }
}
