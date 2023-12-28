package com.github.monsterhxw.rpc.netty.serialize.impl;

import com.github.monsterhxw.rpc.netty.client.stub.RpcRequest;
import com.github.monsterhxw.rpc.netty.serialize.Serializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author huangxuewei
 * @since 2023/12/28
 */
public class RpcRequestSerializer implements Serializer<RpcRequest> {

    @Override
    public int size(RpcRequest rpcRequest) {
        int nameLength = 4 + getStringBytes(rpcRequest.getInterfaceName()).length;
        int methodLength = 4 + getStringBytes(rpcRequest.getMethodName()).length;
        int argsLength = 4 + rpcRequest.getSerializedArguments().length;
        return nameLength + methodLength + argsLength;
    }

    @Override
    public void serialize(RpcRequest rpcRequest, byte[] bytes, int offset, int length) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, offset, length);

        byte[] nameBytes = getStringBytes(rpcRequest.getInterfaceName());
        byteBuffer.putInt(nameBytes.length);
        byteBuffer.put(nameBytes);

        byte[] methodBytes = getStringBytes(rpcRequest.getMethodName());
        byteBuffer.putInt(methodBytes.length);
        byteBuffer.put(methodBytes);

        byteBuffer.putInt(rpcRequest.getSerializedArguments().length);
        byteBuffer.put(rpcRequest.getSerializedArguments());
    }

    @Override
    public RpcRequest deserialize(byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);

        int nameLength = buffer.getInt();
        byte[] nameBytes = new byte[nameLength];
        buffer.get(nameBytes);
        String interfaceName = new String(nameBytes, StandardCharsets.UTF_8);

        int methodLength = buffer.getInt();
        byte[] methodBytes = new byte[methodLength];
        buffer.get(methodBytes);
        String methodName = new String(methodBytes, StandardCharsets.UTF_8);

        int argsLength = buffer.getInt();
        byte[] argsBytes = new byte[argsLength];
        buffer.get(argsBytes);
        return new RpcRequest(interfaceName, methodName, argsBytes);
    }

    @Override
    public byte type() {
        return Types.TYPE_RPC_REQUEST;
    }

    @Override
    public Class<RpcRequest> getSerializeClass() {
        return RpcRequest.class;
    }

    private byte[] getStringBytes(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }
}
