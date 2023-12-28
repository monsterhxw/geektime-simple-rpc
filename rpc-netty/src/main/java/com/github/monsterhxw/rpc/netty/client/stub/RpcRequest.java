package com.github.monsterhxw.rpc.netty.client.stub;

/**
 * @author huangxuewei
 * @since 2023/12/28
 */
public class RpcRequest {

    private final String interfaceName;
    private final String methodName;
    private final byte[] serializedArguments;

    public RpcRequest(String interfaceName, String methodName, byte[] serializedArguments) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.serializedArguments = serializedArguments;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public byte[] getSerializedArguments() {
        return serializedArguments;
    }
}
