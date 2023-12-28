package com.github.monsterhxw.rpc.netty.transport;

import com.github.monsterhxw.rpc.netty.transport.command.Command;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public interface Transport extends Closeable {

    CompletableFuture<Command> send(Command request);
}
