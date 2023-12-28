package com.github.monsterhxw.rpc.netty.transport.netty.codec;

import com.github.monsterhxw.rpc.netty.client.ServiceTypes;
import com.github.monsterhxw.rpc.netty.client.stub.RpcRequest;
import com.github.monsterhxw.rpc.netty.serialize.SerializeSupport;
import com.github.monsterhxw.rpc.netty.transport.command.Command;
import com.github.monsterhxw.rpc.netty.transport.command.Header;
import com.github.monsterhxw.rpc.netty.transport.command.ResponseHeader;

/**
 * @author huangxuewei
 * @since 2023/12/28
 */
public class CommandTestSupport {

    static final int REQUEST_ID = 0;
    static final int VERSION = 1;
    static final int TYPE = ServiceTypes.TYPE_RPC_REQUEST;
    static final String INTERFACE_NAME = "com.github.monsterhxw.rpc.hello.service.api.HelloService";
    static final String METHOD_NAME = "hello";
    static final String METHOD_ARG = "world";

    public static Header buildHeader() {
        return buildHeader(REQUEST_ID, VERSION, TYPE);
    }

    public static Header buildHeader(int requestId, int version, int type) {
        return new Header(requestId, version, type);
    }

    public static RpcRequest buildRpcRequest() {
        return buildRpcRequest(INTERFACE_NAME, METHOD_NAME, METHOD_ARG);
    }

    public static RpcRequest buildRpcRequest(String interfaceName, String methodName, String methodArg) {
        byte[] serializedArguments = SerializeSupport.serialize(methodArg);
        return new RpcRequest(interfaceName, methodName, serializedArguments);
    }

    public static Command buildRequestCommand() {
        return buildRequestCommand(buildHeader(), buildRpcRequest());
    }

    public static Command buildRequestCommand(RpcRequest rpcRequest) {
        return buildRequestCommand(buildHeader(), rpcRequest);
    }

    public static Command buildRequestCommand(Header header, RpcRequest rpcRequest) {
        byte[] payload = SerializeSupport.serialize(rpcRequest);
        return new Command(header, payload);
    }

    public static Command buildResponseCommand(String result) {
        ResponseHeader responseHeader = new ResponseHeader(REQUEST_ID, VERSION, TYPE);
        byte[] payload = SerializeSupport.serialize(result);
        return new Command(responseHeader, payload);
    }
}
