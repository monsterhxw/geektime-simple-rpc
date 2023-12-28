package com.github.monsterhxw.rpc.netty.transport.netty.codec;

import com.github.monsterhxw.rpc.netty.transport.command.Header;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public class RequestDecoder extends CommandDecoder {

    @Override
    protected Header decodeHeader(ChannelHandlerContext ctx, ByteBuf inByteBuf) {
        int requestId = inByteBuf.readInt();
        int version = inByteBuf.readInt();
        int type = inByteBuf.readInt();
        return new Header(requestId, version, type);
    }
}
