package com.github.monsterhxw.rpc.netty.transport.netty;

import com.github.monsterhxw.rpc.netty.transport.RequestHandlerRegistry;
import com.github.monsterhxw.rpc.netty.transport.TransportServer;
import com.github.monsterhxw.rpc.netty.transport.netty.codec.RequestDecoder;
import com.github.monsterhxw.rpc.netty.transport.netty.codec.ResponseEncoder;
import com.github.monsterhxw.rpc.netty.transport.netty.handler.RequestInvocationHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public class NettyServer implements TransportServer {

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    private int port;

    private EventLoopGroup acceptorGroup = new NioEventLoopGroup(1);

    private EventLoopGroup ioWorkerGroup = new NioEventLoopGroup();

    private ServerBootstrap serverBootstrap;

    @Override
    public void start(RequestHandlerRegistry requestHandlerRegistry, int port) throws Exception {
        this.port = port;
        this.serverBootstrap = getAndCreateServerBootstrap(port, requestHandlerRegistry);

        doBindAndListen(serverBootstrap);
    }

    @Override
    public void stop() {
        // gracefully shutdown netty server
        if (serverBootstrap != null) {
            serverBootstrap.config().group().shutdownGracefully().addListener(cf -> {
                if (cf.isSuccess()) {
                    log.info("Netty server group stopped.");
                }
            });
            serverBootstrap.config().childGroup().shutdownGracefully().addListener(cf -> {
                if (cf.isSuccess()) {
                    log.info("Netty server child group stopped.");
                }
            });
            serverBootstrap = null;
        }
    }

    private ServerBootstrap getAndCreateServerBootstrap(int port, RequestHandlerRegistry requestHandlerRegistry) {
        if (serverBootstrap != null) {
            return serverBootstrap;
        }
        serverBootstrap = new ServerBootstrap()
                .group(acceptorGroup, ioWorkerGroup)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, false)
                .localAddress(new InetSocketAddress(port))
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new RequestDecoder())
                                .addLast(new ResponseEncoder())
                                .addLast(new RequestInvocationHandler(requestHandlerRegistry));
                    }
                });
        return serverBootstrap;
    }

    private boolean useEpoll() {
        return Epoll.isAvailable();
    }

    private ChannelFuture doBindAndListen(ServerBootstrap serverBootstrap) {
        try {
            return serverBootstrap.bind().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException("Server bind and listen failed.", e);
        }
    }
}
