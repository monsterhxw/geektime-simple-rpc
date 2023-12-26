package com.github.monsterhxw.rpc.netty.client;

import com.github.monsterhxw.rpc.netty.transport.Transport;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public interface StubFactory {

    <T> T createStub(Transport transport, Class<T> serviceClass);
}
