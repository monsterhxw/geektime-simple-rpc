package com.github.monsterhxw.rpc.api;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

/**
 * @author huangxuewei
 * @since 2023/12/26
 */
public interface NameService {

    Collection<String> supportedSchemes();

    void connect(URI nameServiceUri);

    void registerService(String serviceName, URI uri) throws IOException;

    URI lookupService(String serviceName) throws IOException;

    void close();
}
