package com.github.monsterhxw.rpc.api;

import com.github.monsterhxw.rpc.api.spi.ServiceSupport;

import java.io.Closeable;
import java.net.URI;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * @author huangxuewei
 * @since 2023/12/26
 */
public interface RpcAccessPoint extends Closeable {

    default NameService getNameService(URI nameServiceUri) {
        for (NameService nameService : ServiceSupport.loadAll(NameService.class)) {
            if (nameService.supportedSchemes().contains(nameServiceUri.getScheme())) {
                nameService.connect(nameServiceUri);
                return nameService;
            }
        }
        throw new IllegalArgumentException("Unsupported scheme: " + nameServiceUri.getScheme());
    }

    <T> T getRemoteService(URI uri, Class<T> serviceClass);

    URI getServerUri();

    <T> URI addServiceProvider(T service, Class<T> serviceClass);

    Closeable startServer() throws Exception;
}
