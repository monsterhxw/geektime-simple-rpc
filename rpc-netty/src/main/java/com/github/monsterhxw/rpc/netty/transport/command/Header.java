package com.github.monsterhxw.rpc.netty.transport.command;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author huangxuewei
 * @since 2023/12/26
 */
public class Header {

    private int requestId;
    private int version;
    private int type;

    private static AtomicInteger REQUEST_ID = new AtomicInteger(0);

    public Header(int type) {
        this(REQUEST_ID.getAndIncrement(), 1, type);
    }

    public Header(int version, int type) {
        this(REQUEST_ID.getAndIncrement(), version, type);
    }

    public Header(int requestId, int version, int type) {
        this.requestId = requestId;
        this.version = version;
        this.type = type;
    }

    /**
     * @return length in Byte
     */
    public int length() {
        return 12;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Header{");
        sb.append("requestId=").append(requestId);
        sb.append(", version=").append(version);
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }
}
