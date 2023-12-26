package com.github.monsterhxw.rpc.netty.transport;

import com.github.monsterhxw.rpc.netty.transport.command.Command;

/**
 * @author huangxuewei
 * @since 2023/12/26
 */
public interface RequestHandler {

    Command handle(Command requestCommand);

    int type();
}
