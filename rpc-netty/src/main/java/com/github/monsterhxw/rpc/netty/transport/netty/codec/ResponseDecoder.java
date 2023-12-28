package com.github.monsterhxw.rpc.netty.transport.netty.codec;

import com.github.monsterhxw.rpc.netty.transport.command.Header;
import com.github.monsterhxw.rpc.netty.transport.command.ResponseHeader;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * @author huangxuewei
 * @since 2023/12/29
 */
public class ResponseDecoder extends CommandDecoder {

    @Override
    protected Header decodeHeader(ByteBuf inByteBuf) {
        int requestId = inByteBuf.readInt();
        int version = inByteBuf.readInt();
        int type = inByteBuf.readInt();

        int code = inByteBuf.readInt();

        int errorLength = inByteBuf.readInt();
        byte[] errorBytes = new byte[errorLength];
        inByteBuf.readBytes(errorBytes);
        String error = new String(errorBytes, StandardCharsets.UTF_8);

        return new ResponseHeader(requestId, version, type, code, error);
    }
}
