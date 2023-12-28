package com.github.monsterhxw.rpc.netty.transport.netty.codec;

import com.github.monsterhxw.rpc.netty.transport.command.Header;
import com.github.monsterhxw.rpc.netty.transport.command.ResponseHeader;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public class ResponseEncoder extends CommandEncoder {

    @Override
    protected void encodeHeader(Header header, ByteBuf outByteBuf) throws Exception {
        super.encodeHeader(header, outByteBuf);

        if (!(header instanceof ResponseHeader)) {
            throw new Exception(String.format("Invalid header type: [%s] is not instance of [%s]", header.getClass().getCanonicalName(), ResponseHeader.class.getName()));
        }

        ResponseHeader respHeader = (ResponseHeader) header;
        outByteBuf.writeInt(respHeader.getCode());

        int errorLen = respHeader.getErrorLength();
        outByteBuf.writeInt(errorLen);

        byte[] errorBytes = respHeader.getError() == null ? new byte[0] : respHeader.getError().getBytes(StandardCharsets.UTF_8);
        outByteBuf.writeBytes(errorBytes);
    }
}
