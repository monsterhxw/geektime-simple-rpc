package com.github.monsterhxw.rpc.netty.transport;

import com.github.monsterhxw.rpc.netty.transport.command.Command;

import java.util.concurrent.CompletableFuture;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public interface Transport {

    CompletableFuture<Command> send(Command request);
}
