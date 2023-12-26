package com.github.monsterhxw.rpc.server;

import com.github.monsterhxw.rpc.hello.service.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public class HelloServiceImpl implements HelloService {

    private static final Logger LOG = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(String name) {
        LOG.info("HelloServiceImpl receive message: {}!", name);
        String res = "Hello " + name;
        LOG.info("HelloServiceImpl return result: {}!", res);
        return res;
    }
}
