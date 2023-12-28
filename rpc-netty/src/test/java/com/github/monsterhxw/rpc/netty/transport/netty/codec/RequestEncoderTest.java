package com.github.monsterhxw.rpc.netty.transport.netty.codec;

import com.github.monsterhxw.rpc.netty.client.stub.RpcRequest;
import com.github.monsterhxw.rpc.netty.serialize.SerializeSupport;
import com.github.monsterhxw.rpc.netty.transport.command.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author huangxuewei
 * @since 2023/12/28
 */
class RequestEncoderTest {

    private RequestEncoder requestEncoder;
    private RpcRequest rpcRequest;
    private Command requestCommand;
    private ByteBuf outByteBuf;

    @BeforeEach
    void setUp() {
        this.requestEncoder = new RequestEncoder();
        this.rpcRequest = CommandTestSupport.buildRpcRequest();
        this.requestCommand = CommandTestSupport.buildRequestCommand(rpcRequest);
        this.outByteBuf = ByteBufAllocator.DEFAULT.ioBuffer();
    }

    @AfterEach
    void tearDown() {
        this.outByteBuf.clear();
        this.outByteBuf = null;
        this.requestEncoder = null;
        this.rpcRequest = null;
        this.requestCommand = null;
    }

    @Test
    void encode() throws Exception {
        this.requestEncoder.encode(null, this.requestCommand, this.outByteBuf);

        int lengthFileValue = this.outByteBuf.readInt();
        int expectedLengthFileValue = 4 + this.requestCommand.getHeader().length() + this.requestCommand.getPayload().length;
        assertEquals(expectedLengthFileValue, lengthFileValue);

        int requestId = this.outByteBuf.readInt();
        int expectedRequestId = this.requestCommand.getHeader().getRequestId();
        assertEquals(expectedRequestId, requestId);

        int version = this.outByteBuf.readInt();
        int expectedVersion = this.requestCommand.getHeader().getVersion();
        assertEquals(expectedVersion, version);

        int type = this.outByteBuf.readInt();
        int expectedType = this.requestCommand.getHeader().getType();
        assertEquals(expectedType, type);

        byte[] payload = new byte[this.requestCommand.getPayload().length];
        this.outByteBuf.readBytes(payload);
        assertEquals(this.requestCommand.getPayload().length, payload.length);

        RpcRequest actualRpcRequest = SerializeSupport.deserialize(payload);
        assertEquals(this.rpcRequest.getInterfaceName(), actualRpcRequest.getInterfaceName());
        assertEquals(this.rpcRequest.getMethodName(), actualRpcRequest.getMethodName());
        assertEquals(this.rpcRequest.getSerializedArguments().length, actualRpcRequest.getSerializedArguments().length);

        String actualMethodArg = SerializeSupport.deserialize(actualRpcRequest.getSerializedArguments());
        String expectedMethodArg = SerializeSupport.deserialize(this.rpcRequest.getSerializedArguments());
        assertEquals(expectedMethodArg, actualMethodArg);
    }
}