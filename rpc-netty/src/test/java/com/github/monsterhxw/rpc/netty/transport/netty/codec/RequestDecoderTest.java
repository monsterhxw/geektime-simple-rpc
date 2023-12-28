package com.github.monsterhxw.rpc.netty.transport.netty.codec;

import com.github.monsterhxw.rpc.netty.client.stub.RpcRequest;
import com.github.monsterhxw.rpc.netty.serialize.SerializeSupport;
import com.github.monsterhxw.rpc.netty.transport.command.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author huangxuewei
 * @since 2023/12/28
 */
class RequestDecoderTest {

    private ByteBuf inByteBuf;

    private RequestDecoder requestDecoder;
    private RpcRequest rpcRequest;
    private Command requestCommand;

    @BeforeEach
    void setUp() throws Exception {
        this.requestDecoder = new RequestDecoder();
        this.rpcRequest = CommandTestSupport.buildRpcRequest();
        this.requestCommand = CommandTestSupport.buildRequestCommand(rpcRequest);

        this.inByteBuf = ByteBufAllocator.DEFAULT.ioBuffer();

        encodeRequestCommandToByteBuf(this.requestCommand, this.inByteBuf);
    }

    private void encodeRequestCommandToByteBuf(Command requestCommand, ByteBuf inByteBuf) throws Exception {
        new RequestEncoder().encode(null, requestCommand, inByteBuf);
    }

    @AfterEach
    void tearDown() {
        this.inByteBuf.clear();
        this.inByteBuf = null;
        this.requestDecoder = null;
        this.rpcRequest = null;
        this.requestCommand = null;
    }

    @Test
    void decode() throws Exception {
        ArrayList<Object> outList = new ArrayList<>();

        this.requestDecoder.decode(null, this.inByteBuf, outList);

        assertEquals(1, outList.size());
        Object o = outList.get(0);
        assertEquals(Command.class, o.getClass());
        Command command = (Command) o;

        assertNotNull(command.getHeader());

        assertEquals(this.requestCommand.getHeader().getRequestId(), command.getHeader().getRequestId());
        assertEquals(this.requestCommand.getHeader().getVersion(), command.getHeader().getVersion());
        assertEquals(this.requestCommand.getHeader().getType(), command.getHeader().getType());
        assertEquals(this.requestCommand.getPayload().length, command.getPayload().length);

        Object payload = SerializeSupport.deserialize(command.getPayload());
        assertNotNull(payload);
        assertEquals(RpcRequest.class, payload.getClass());

        RpcRequest rpcRequest = (RpcRequest) payload;
        assertEquals(this.rpcRequest.getInterfaceName(), rpcRequest.getInterfaceName());
        assertEquals(this.rpcRequest.getMethodName(), rpcRequest.getMethodName());
        assertEquals(this.rpcRequest.getSerializedArguments().length, rpcRequest.getSerializedArguments().length);

        Object actualArgs = SerializeSupport.deserialize(rpcRequest.getSerializedArguments());
        assertNotNull(actualArgs);
        assertEquals(String.class, actualArgs.getClass());

        String actualArgsStr = (String) actualArgs;
        String expectArgStr = SerializeSupport.deserialize(this.rpcRequest.getSerializedArguments());
        assertEquals(expectArgStr, actualArgsStr);
    }
}