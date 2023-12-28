package com.github.monsterhxw.rpc.netty.transport.netty.codec;

import com.github.monsterhxw.rpc.netty.serialize.SerializeSupport;
import com.github.monsterhxw.rpc.netty.transport.command.Command;
import com.github.monsterhxw.rpc.netty.transport.command.ResponseHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author huangxuewei
 * @since 2023/12/29
 */
class ResponseDecoderTest {

    private ResponseDecoder responseDecoder;

    private Command responseCommand;

    private final String result = "Hello, Test";

    private ByteBuf inByteBuf;

    @BeforeEach
    void setUp() throws Exception {
        this.responseDecoder = new ResponseDecoder();

        this.responseCommand = CommandTestSupport.buildResponseCommand(this.result);

        this.inByteBuf = ByteBufAllocator.DEFAULT.ioBuffer();

        encodeResponseCommandToByteBuf(this.responseCommand, this.inByteBuf);
    }

    private void encodeResponseCommandToByteBuf(Command responseCommand, ByteBuf inByteBuf) throws Exception {
        new ResponseEncoder().encode(null, responseCommand, inByteBuf);
    }

    @AfterEach
    void tearDown() {
        this.responseDecoder = null;

        this.responseCommand = null;

        this.inByteBuf.clear();
        this.inByteBuf = null;
    }

    @Test
    void decode() throws Exception {
        ArrayList<Object> outList = new ArrayList<>();

        this.responseDecoder.decode(null, this.inByteBuf, outList);

        assertEquals(1, outList.size());
        Object o = outList.get(0);
        assertEquals(Command.class, o.getClass());
        Command command = (Command) o;

        assertNotNull(command.getHeader());

        assertEquals(this.responseCommand.getHeader().getRequestId(), command.getHeader().getRequestId());
        assertEquals(this.responseCommand.getHeader().getVersion(), command.getHeader().getVersion());
        assertEquals(this.responseCommand.getHeader().getType(), command.getHeader().getType());

        assertTrue(command.getHeader() instanceof ResponseHeader);

        ResponseHeader actualRespHeader = (ResponseHeader) command.getHeader();
        ResponseHeader expectRespHeader = (ResponseHeader) this.responseCommand.getHeader();
        assertEquals(expectRespHeader.getCode(), actualRespHeader.getCode());
        assertEquals(expectRespHeader.getErrorLength(), actualRespHeader.getErrorLength());
        assertEquals(expectRespHeader.getError(), actualRespHeader.getError());

        assertEquals(this.responseCommand.getPayload().length, command.getPayload().length);
        Object payload = SerializeSupport.deserialize(command.getPayload());
        assertNotNull(payload);
        assertEquals(String.class, payload.getClass());

        String actualResult = (String) payload;
        assertEquals(this.result, actualResult);
    }
}