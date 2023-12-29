package com.github.monsterhxw.rpc.netty.client.stub;

import com.github.monsterhxw.rpc.netty.client.ServiceTypes;
import com.github.monsterhxw.rpc.netty.serialize.SerializeSupport;
import com.github.monsterhxw.rpc.netty.transport.Transport;
import com.github.monsterhxw.rpc.netty.transport.command.Code;
import com.github.monsterhxw.rpc.netty.transport.command.Command;
import com.github.monsterhxw.rpc.netty.transport.command.Header;
import com.github.monsterhxw.rpc.netty.transport.command.ResponseHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author huangxuewei
 * @since 2023/12/30
 */
public class InvokerInvocationHandler implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(InvokerInvocationHandler.class);

    private final Transport transport;

    public InvokerInvocationHandler(Transport transport) {
        this.transport = transport;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(transport, args);
        }

        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return transport.toString();
        }
        if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
            return transport.hashCode();
        }
        if ("equals".equals(methodName) && parameterTypes.length == 1) {
            return transport.equals(args[0]);
        }

        String interfaceName = proxy.getClass().getInterfaces()[0].getCanonicalName();

        // TODO only support one parameter and the parameter type is String now
        if (args.length != 1 || parameterTypes[0] != String.class) {
            throw new IllegalArgumentException("Now only support one parameter and the parameter type is String");
        }
        String arg = (String) args[0];
        return invoke(new RpcRequest(interfaceName, methodName, SerializeSupport.serialize(arg)));
    }

    private Object invoke(RpcRequest rpcRequest) {
        Header header = new Header(ServiceTypes.TYPE_RPC_REQUEST);
        byte[] payload = SerializeSupport.serialize(rpcRequest);
        Command requestCommand = new Command(header, payload);
        try {
            Command respondCommand = transport.send(requestCommand).get(transport.getTimeoutMillis(), TimeUnit.MILLISECONDS);
            ResponseHeader respHeader = (ResponseHeader) respondCommand.getHeader();
            if (respHeader.getCode() == Code.SUCCESS.getCode()) {
                return SerializeSupport.deserialize(respondCommand.getPayload());
            } else {
                throw new Exception(respHeader.getError());
            }
        } catch (InterruptedException e) {
            log.warn("Send request interrupted, request: {}, header: {}", rpcRequest, header);
            throw new RuntimeException("Send request interrupted", e);
        } catch (ExecutionException e) {
            log.warn("Send request execution error, request: {}, header: {}", rpcRequest, header);
            throw new RuntimeException("Send request execution error", e);
        } catch (TimeoutException e) {
            log.warn("Send request timeout, request: {}, header: {}", rpcRequest, header);
            throw new RuntimeException("Send request timeout", e);
        } catch (Throwable e) {
            log.warn("Send request error, request: {}, header: {}", rpcRequest, header);
            throw new RuntimeException("Send request error", e);
        }
    }
}
