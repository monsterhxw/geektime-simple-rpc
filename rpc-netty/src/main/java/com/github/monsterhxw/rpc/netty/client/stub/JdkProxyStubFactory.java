package com.github.monsterhxw.rpc.netty.client.stub;

import com.github.monsterhxw.rpc.netty.transport.Transport;

import java.lang.reflect.Proxy;

/**
 * @author huangxuewei
 * @since 2023/12/29
 */
public class JdkProxyStubFactory implements StubFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createStub(Transport transport, Class<?>[] interfaces) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, new InvokerInvocationHandler(transport));
    }
}
