package com.github.monsterhxw.rpc.netty.transport.netty.codec;

import com.github.monsterhxw.rpc.netty.serialize.SerializeSupport;
import com.github.monsterhxw.rpc.netty.transport.command.Command;
import com.github.monsterhxw.rpc.netty.transport.command.ResponseHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author huangxuewei
 * @since 2023/12/29
 */
class ResponseEncoderTest {

    private ResponseEncoder responseEncoder;

    private ByteBuf outByteBuf;

    private Command responseCommand;

    private final String result = "Hello, Test";

    @BeforeEach
    void setUp() {
        this.responseEncoder = new ResponseEncoder();

        this.outByteBuf = ByteBufAllocator.DEFAULT.ioBuffer();

        this.responseCommand = CommandTestSupport.buildResponseCommand(this.result);
    }

    @AfterEach
    void tearDown() {
        this.responseEncoder = null;

        this.outByteBuf.clear();
        this.outByteBuf = null;

        this.responseCommand = null;
    }

    @Test
    void encode() throws Exception {
        this.responseEncoder.encode(null, this.responseCommand, this.outByteBuf);

        int actualLengthFileValue = this.outByteBuf.readInt();
        int expectedLengthFileValue = 4 + this.responseCommand.getHeader().length() + this.responseCommand.getPayload().length;
        assertEquals(expectedLengthFileValue, actualLengthFileValue);

        int actualRequestId = this.outByteBuf.readInt();
        int expectedRequestId = this.responseCommand.getHeader().getRequestId();
        assertEquals(expectedRequestId, actualRequestId);

        int actualVersion = this.outByteBuf.readInt();
        int expectedVersion = this.responseCommand.getHeader().getVersion();
        assertEquals(expectedVersion, actualVersion);

        int actualType = this.outByteBuf.readInt();
        int expectedType = this.responseCommand.getHeader().getType();
        assertEquals(expectedType, actualType);

        int actualResponseCode = this.outByteBuf.readInt();
        ResponseHeader respHeader = (ResponseHeader) this.responseCommand.getHeader();
        int expectedResponseCode = respHeader.getCode();
        assertEquals(expectedResponseCode, actualResponseCode);

        int actualErrorMsgLength = this.outByteBuf.readInt();
        int expectedErrorMsgLength = respHeader.getErrorLength();
        assertEquals(expectedErrorMsgLength, actualErrorMsgLength);

        byte[] errorBytes = new byte[actualErrorMsgLength];
        this.outByteBuf.readBytes(errorBytes);
        String actualErrorMsg = new String(errorBytes, StandardCharsets.UTF_8);
        if (respHeader.getError() == null) {
            assertEquals("", actualErrorMsg);
        } else {
            assertEquals(respHeader.getError(), actualErrorMsg);
        }

        byte[] payload = new byte[this.responseCommand.getPayload().length];
        this.outByteBuf.readBytes(payload);
        assertEquals(this.responseCommand.getPayload().length, payload.length);

        String actualResult = SerializeSupport.deserialize(payload);
        assertEquals(this.result, actualResult);
    }
}