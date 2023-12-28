package com.github.monsterhxw.rpc.netty.transport.netty;

import com.github.monsterhxw.rpc.netty.transport.Transport;
import com.github.monsterhxw.rpc.netty.transport.command.Command;
import com.github.monsterhxw.rpc.netty.transport.netty.codec.RequestEncoder;
import com.github.monsterhxw.rpc.netty.transport.netty.codec.ResponseDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author huangxuewei
 * @since 2023/12/29
 */
public class NettyTransport implements Transport {

    private static final Logger log = LoggerFactory.getLogger(NettyTransport.class);

    private final Bootstrap bootstrap;

    private final int connectionTimeout;

    private final SocketAddress address;

    private final ConcurrentHashMap<String/* address */, ChannelFuture> channelTables = new ConcurrentHashMap<>();

    private final Lock lockChannelTables = new ReentrantLock();

    private static final long LOCK_TIMEOUT_MILLIS = 3_000L;

    public NettyTransport(SocketAddress address, int connectionTimeout) {
        this.address = address;

        connectionTimeout = connectionTimeout <= 0 ? 1_000 : connectionTimeout;
        connectionTimeout = Math.min(connectionTimeout, 5_000);
        this.connectionTimeout = connectionTimeout;

        NioEventLoopGroup ioWorkerGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap()
                .group(ioWorkerGroup)
                .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new ResponseDecoder())
                                .addLast(new RequestEncoder())
//                                        .addLast(ResponseInvocation.getInstance())
                        ;
                    }
                });

//        String addr = address.toString();
//        Channel channel = this.getAndCreateChannel(address, connectionTimeout);
//        if (null != channel && channel.isActive()) {
//
//        } else {
//            this.closeChannelAndRemoveFromChannelTables(addr, channel);
//            throw new RemotingConnectionException(addr);
//        }
//        return null;

    }

    @Override
    public CompletableFuture<Command> send(Command request) {
        return null;
    }

    private Channel getAndCreateChannel(SocketAddress address) throws InterruptedException {
        ChannelFuture channelFuture = this.channelTables.get(address.toString());
        if (isChannelOK(channelFuture)) {
            return channelFuture.channel();
        }
        return createChannel(address);
    }

    private boolean isChannelOK(ChannelFuture channelFuture) {
        return null != channelFuture && null != channelFuture.channel() && channelFuture.channel().isActive();
    }

    private Channel createChannel(SocketAddress address) throws InterruptedException {
        String addr = address.toString();
        ChannelFuture channelFuture = this.channelTables.get(addr);
        if (isChannelOK(channelFuture)) {
            return channelFuture.channel();
        }

        if (this.lockChannelTables.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            try {
                boolean isNew = true;
                channelFuture = this.channelTables.get(addr);
                if (channelFuture != null) {
                    if (isChannelOK(channelFuture)) {
                        return channelFuture.channel();
                    } else if (!channelFuture.isDone()) {
                        isNew = false;
                    } else {
                        this.channelTables.remove(addr);
                    }
                }
                if (isNew) {
                    channelFuture = this.bootstrap.connect(address);
                    this.channelTables.put(addr, channelFuture);
                }
            } catch (Exception e) {
                log.error("createChannel: failed to create channel, caused by: ", e);
            } finally {
                this.lockChannelTables.unlock();
            }
        } else {
            log.warn("createChannel: try lock channel table timeout, but timeout {} ms", LOCK_TIMEOUT_MILLIS);
        }

        if (null != channelFuture && channelFuture.awaitUninterruptibly(this.connectionTimeout)) {
            if (isChannelOK(channelFuture)) {
                log.info("createChannel: success to connect remote host {}", address);
                return channelFuture.channel();
            } else {
                log.error("createChannel: failed to connect remote host {}，caused by: ", address, channelFuture.cause());
            }
        }

        return null;
    }

    private void closeChannelAndRemoveFromChannelTables(String address, Channel channel) {
        if (null == channel) {
            return;
        }

        try {
            if (this.lockChannelTables.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                try {
                    boolean removeFromTable = true;
                    ChannelFuture prevChannelFuture = this.channelTables.get(address);
                    log.info("closeChannel: try to close channel, address: {}, channel: {}, Found: {}", address, channel, null != prevChannelFuture);

                    if (null == prevChannelFuture) {
                        log.info("closeChannel: the channel[{}] has been removed from the channel table before", address);
                        removeFromTable = false;
                    } else if (prevChannelFuture.channel() != channel) {
                        log.info("closeChannel: the channel[{}] has been closed before, and has been created again, nothing to do.", address);
                        removeFromTable = false;
                    }

                    if (removeFromTable) {
                        this.channelTables.remove(address);
                        log.info("closeChannel: the channel [{}] was removed from the channel table.", address);
                    }

                    channel.close()
                            .addListener(f -> log.info("closeChannel: close the connection to remove address[{}] result: {}", address, f.isSuccess()));
                } catch (Exception e) {
                    log.error("closeChannel: failed to close channel, caused by: ", e);
                } finally {
                    this.lockChannelTables.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("closeChannel: failed to close channel, caused by: ", e);
        }
    }

    @Override
    public void close() throws IOException {
        if (null != bootstrap) {
            bootstrap.group()
                    .shutdownGracefully()
                    .addListener(f -> log.info("Netty client group shutdown."));
        }
        if (!channelTables.isEmpty()) {
            for (Map.Entry<String, ChannelFuture> entry : channelTables.entrySet()) {
                closeChannelAndRemoveFromChannelTables(entry.getKey(), entry.getValue().channel());
            }
            channelTables.clear();
        }
    }
}
