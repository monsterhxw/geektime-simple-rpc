package com.github.monsterhxw.rpc.netty.transport.netty.handler;

import com.github.monsterhxw.rpc.netty.transport.RequestHandler;
import com.github.monsterhxw.rpc.netty.transport.RequestHandlerRegistry;
import com.github.monsterhxw.rpc.netty.transport.command.Code;
import com.github.monsterhxw.rpc.netty.transport.command.Command;
import com.github.monsterhxw.rpc.netty.transport.command.CommandSupport;
import com.github.monsterhxw.rpc.netty.transport.command.ResponseHeader;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
@ChannelHandler.Sharable
public class RequestInvocationHandler extends SimpleChannelInboundHandler<Command> {

    private static final Logger log = LoggerFactory.getLogger(RequestInvocationHandler.class);

    private RequestHandlerRegistry requestHandlerRegistry;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command request) throws Exception {
        Command errorResponse = checkRequestHandlerRegistryOrElseReturnError(request);
        if (errorResponse != null) {
            ctx.writeAndFlush(errorResponse);
        } else {
            RequestHandler requestHandler = requestHandlerRegistry.get(request.getHeader().getType());
            Command response = requestHandler.handle(request);
            if (null == response) {
                log.warn("response is null, requestId: {}, version: {}, type: {}.", request.getHeader().getRequestId(), request.getHeader().getVersion(), request.getHeader().getType());
                return;
            }
            ctx.writeAndFlush(response).addListener(cf -> {
                if (!cf.isSuccess()) {
                    log.error("response failed, requestId: {}, version: {}, type: {}.", request.getHeader().getRequestId(), request.getHeader().getVersion(), request.getHeader().getType());
                    ctx.channel().close();
                }
            });
        }
    }

    private Command checkRequestHandlerRegistryOrElseReturnError(Command request) {
        int requestId = request.getHeader().getRequestId();
        int version = request.getHeader().getVersion();
        int type = request.getHeader().getType();

        if (requestHandlerRegistry == null) {
            log.error("requestHandlerRegistry is null, requestId: {}, version: {}, type: {}", requestId, version, type);
            return CommandSupport.errorRespCommand(request.getHeader(), Code.UNKNOWN_ERROR, "Server Internal Error");
        }

        RequestHandler requestHandler = requestHandlerRegistry.get(request.getHeader().getType());
        if (null == requestHandler) {
            log.error("Unsupported Type: {}, requestId: {}, version: {}.", type, requestId, version);
            return CommandSupport.errorRespCommand(request.getHeader(), Code.UNSUPPORTED_TYPE, "Unsupported Type: " + type);
        }

        return null;
    }

    public static RequestInvocationHandler getInstance() {
        return RequestInvocationHandlerHolder.INSTANCE;
    }

    public void setRequestHandlerRegistry(RequestHandlerRegistry requestHandlerRegistry) {
        this.requestHandlerRegistry = requestHandlerRegistry;
    }

    private static class RequestInvocationHandlerHolder {
        private static final RequestInvocationHandler INSTANCE = new RequestInvocationHandler();
    }
}
