package com.github.monsterhxw.rpc.netty.transport.command;

/**
 * @author huangxuewei
 * @since 2023/12/28
 */
public class CommandSupport {

    public static Command errorRespCommand(Header header, Code code, String errorMsg) {
        return errorRespCommand(header.getRequestId(), header.getVersion(), header.getType(), code, errorMsg);
    }

    public static Command errorRespCommand(Header header, Code code) {
        return errorRespCommand(header.getRequestId(), header.getVersion(), header.getType(), code, null);
    }

    public static Command errorRespCommand(int requestId, int version, int type, Code code, String errorMsg) {
        ResponseHeader respHeader = new ResponseHeader(requestId, version, type, code.getCode(), errorMsg == null || errorMsg.isEmpty() ? code.getMessage() : errorMsg);
        return new Command(respHeader, new byte[0]);
    }

    public static Command successRespCommand(Header header, byte[] payload) {
        return successRespCommand(header.getRequestId(), header.getVersion(), header.getType(), payload);
    }

    public static Command successRespCommand(int requestId, int version, int type, byte[] payload) {
        ResponseHeader responseHeader = new ResponseHeader(requestId, version, type);
        return new Command(responseHeader, payload);
    }
}
