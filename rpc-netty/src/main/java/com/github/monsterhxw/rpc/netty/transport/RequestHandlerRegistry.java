package com.github.monsterhxw.rpc.netty.transport;

import com.github.monsterhxw.rpc.api.spi.ServiceSupport;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author huangxuewei
 * @since 2023/12/26
 */
public class RequestHandlerRegistry {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(RequestHandlerRegistry.class);

    private ConcurrentHashMap<Integer, RequestHandler> handlerMap = new ConcurrentHashMap<>();

    public static RequestHandlerRegistry getInstance() {
        return RequestHandlerRegistryHolder.INSTANCE;
    }

    private static class RequestHandlerRegistryHolder {
        private static final RequestHandlerRegistry INSTANCE = new RequestHandlerRegistry();
    }

    private RequestHandlerRegistry() {
        ServiceSupport.loadAll(RequestHandler.class).forEach(this::register);
    }

    public void register(RequestHandler requestHandler) {
        handlerMap.put(requestHandler.type(), requestHandler);
        log.info("Load request handler, type: {}, class: {}.", requestHandler.type(), requestHandler.getClass().getCanonicalName());
    }

    public RequestHandler get(int type) {
        return handlerMap.get(type);
    }
}
