package com.github.monsterhxw.rpc.api.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author huangxuewei
 * @since 2023/12/26
 */
public class ServiceSupport {

    private static final Logger log = LoggerFactory.getLogger(ServiceSupport.class);

    private ServiceSupport() {
    }

    private final static ConcurrentHashMap<String, Object> SERVICE_TABLE = new ConcurrentHashMap<>();

    public static synchronized <S> S load(Class<S> serviceClass) throws ServiceLoadException {
        log.info("Load service, class: {}.", serviceClass.getCanonicalName());
        return StreamSupport
                .stream(ServiceLoader.load(serviceClass).spliterator(), false)
                .map(ServiceSupport::singletonFilter)
                .findFirst()
                .orElseThrow(ServiceLoadException::new);
    }

    public static synchronized <S> Collection<S> loadAll(Class<S> serviceClass) {
        log.info("Load all services, class: {}.", serviceClass.getCanonicalName());
        return StreamSupport
                .stream(ServiceLoader.load(serviceClass).spliterator(), false)
                .map(ServiceSupport::singletonFilter)
                .collect(Collectors.toList());
    }

    private static <S> S singletonFilter(S service) {
        if (service.getClass().isAnnotationPresent(Singleton.class)) {
            String canonicalName = service.getClass().getCanonicalName();
            Object prevService = SERVICE_TABLE.putIfAbsent(canonicalName, service);
            return prevService == null ? service : (S) prevService;
        } else {
            return service;
        }
    }
}
