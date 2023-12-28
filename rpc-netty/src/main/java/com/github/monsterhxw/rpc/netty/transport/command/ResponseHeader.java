package com.github.monsterhxw.rpc.netty.transport.command;

import java.nio.charset.StandardCharsets;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public class ResponseHeader extends Header {

    private int code;

    private String error;

    public ResponseHeader(int requestId, int version, int type) {
        this(requestId, version, type, Code.SUCCESS.getCode(), null);
    }

    public ResponseHeader(int requestId, int version, int type, int code, String error) {
        super(requestId, version, type);
        this.code = code;
        if (null != error && !error.isEmpty()) {
            this.error = error;
        }
    }

    public int getErrorLength() {
        return error == null ? 0 : error.getBytes(StandardCharsets.UTF_8).length;
    }

    @Override
    public int length() {
        return super.length() + 4 + getErrorLength();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
