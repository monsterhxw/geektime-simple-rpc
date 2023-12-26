package com.github.monsterhxw.rpc.netty.transport;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public interface TransportServer {

    void start(RequestHandlerRegistry requestHandlerRegistry, int port) throws Exception;

    void stop();
}
