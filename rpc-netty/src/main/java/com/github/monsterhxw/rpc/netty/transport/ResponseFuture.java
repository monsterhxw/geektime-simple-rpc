package com.github.monsterhxw.rpc.netty.transport;

import com.github.monsterhxw.rpc.netty.transport.command.Command;

import java.util.concurrent.CompletableFuture;

/**
 * @author huangxuewei
 * @since 2023/12/30
 */
public class ResponseFuture {

    private final int requestId;

    private final CompletableFuture<Command> future;

    private final long beginTimestamp = System.currentTimeMillis();

    private final long timeoutMillis;

    public ResponseFuture(int requestId, long timeoutMillis, CompletableFuture<Command> future) {
        this.requestId = requestId;
        this.timeoutMillis = timeoutMillis;
        this.future = future;
    }

    public int getRequestId() {
        return this.requestId;
    }

    public CompletableFuture<Command> getFuture() {
        return this.future;
    }

    public long getBeginTimestamp() {
        return this.beginTimestamp;
    }

    public long getTimeoutMillis() {
        return this.timeoutMillis;
    }
}
