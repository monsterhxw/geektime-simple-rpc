package com.github.monsterhxw.rpc.netty.server;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public interface ServiceProviderRegistry {

    <T> void addServiceProvider(Class<? extends T> serviceClass, T serviceProvider);
}
