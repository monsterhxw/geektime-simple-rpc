package com.github.monsterhxw.rpc.netty.transport.netty.codec;

import com.github.monsterhxw.rpc.netty.client.ServiceTypes;
import com.github.monsterhxw.rpc.netty.client.stub.RpcRequest;
import com.github.monsterhxw.rpc.netty.serialize.SerializeSupport;
import com.github.monsterhxw.rpc.netty.transport.command.Command;
import com.github.monsterhxw.rpc.netty.transport.command.Header;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

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
    void setUp() {
        this.requestDecoder = new RequestDecoder();
        this.rpcRequest = buildRpcRequest();
        this.requestCommand = buildRequestCommand(SerializeSupport.serialize(rpcRequest));

        this.inByteBuf = ByteBufAllocator.DEFAULT.ioBuffer();

        int lengthFieldSize = 4 + requestCommand.getHeader().length() + requestCommand.getPayload().length;
        this.inByteBuf.writeInt(lengthFieldSize);

        this.inByteBuf.writeInt(requestCommand.getHeader().getRequestId());
        this.inByteBuf.writeInt(requestCommand.getHeader().getVersion());
        this.inByteBuf.writeInt(requestCommand.getHeader().getType());

        this.inByteBuf.writeBytes(requestCommand.getPayload());
    }

    private Command buildRequestCommand(byte[] payload) {
        int requestId = 0;
        int version = 1;
        int type = ServiceTypes.TYPE_RPC_REQUEST; // 0
        Header requestHdr = new Header(requestId, version, type);
        return new Command(requestHdr, payload);
    }

    private RpcRequest buildRpcRequest() {
        String interfaceName = "com.github.monsterhxw.rpc.hello.service.api.HelloService";
        String methodName = "hello";
        String methodArg = "world";
        byte[] serializedArguments = SerializeSupport.serialize(methodArg);
        return new RpcRequest(interfaceName, methodName, serializedArguments);
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