package com.github.monsterhxw.rpc.netty.transport.netty.codec;

import com.github.monsterhxw.rpc.netty.transport.command.Command;
import com.github.monsterhxw.rpc.netty.transport.command.Header;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public abstract class CommandEncoder extends MessageToByteEncoder<Command> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Command command, ByteBuf outByteBuf) throws Exception {
        int lengthFieldValue = 4 + command.getHeader().length() + command.getPayload().length;
        outByteBuf.writeInt(lengthFieldValue);

        encodeHeader(command.getHeader(), outByteBuf);

        outByteBuf.writeBytes(command.getPayload());
    }

    protected void encodeHeader(Header header, ByteBuf outByteBuf) throws Exception {
        outByteBuf.writeInt(header.getRequestId());
        outByteBuf.writeInt(header.getVersion());
        outByteBuf.writeInt(header.getType());
    }
}
