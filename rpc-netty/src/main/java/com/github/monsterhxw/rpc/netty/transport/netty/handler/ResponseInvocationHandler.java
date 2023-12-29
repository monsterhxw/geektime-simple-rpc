package com.github.monsterhxw.rpc.netty.transport.netty.handler;

import com.github.monsterhxw.rpc.netty.transport.ResponseFuture;
import com.github.monsterhxw.rpc.netty.transport.command.Command;
import com.github.monsterhxw.rpc.netty.transport.InFlightRequestManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author huangxuewei
 * @since 2023/12/30
 */
public class ResponseInvocationHandler extends SimpleChannelInboundHandler<Command> {

    private static final Logger log = LoggerFactory.getLogger(ResponseInvocationHandler.class);

    private final InFlightRequestManager inFlightRequestManager;

    public ResponseInvocationHandler(InFlightRequestManager inFlightRequestManager) {
        this.inFlightRequestManager = inFlightRequestManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command respCommand) throws Exception {
        int requestId = respCommand.getHeader().getRequestId();
        int version = respCommand.getHeader().getVersion();
        int type = respCommand.getHeader().getType();

        ResponseFuture responseFuture = inFlightRequestManager.removeResponseFuture(requestId);

        if (null == responseFuture) {
            log.warn("responseFuture is null, requestId: {}, version: {}, type: {}.", requestId, version, type);
        } else {
            responseFuture.getFuture().complete(respCommand);
        }
    }
}
