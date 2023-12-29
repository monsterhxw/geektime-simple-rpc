package com.github.monsterhxw.rpc.server;

import com.github.monsterhxw.rpc.api.NameService;
import com.github.monsterhxw.rpc.api.RpcAccessPoint;
import com.github.monsterhxw.rpc.api.spi.ServiceSupport;
import com.github.monsterhxw.rpc.hello.service.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public class Bootstrap {

    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    private static final String TMP_DIR_ARG = "java.io.tmpdir";

    private static final String NAME_SERVICE_FILE_NAME = "simple_rpc_name_service.log";

    public static void main(String[] args) {
        String helloServiceName = HelloService.class.getCanonicalName();
        HelloService helloService = new HelloServiceImpl();

        NameService nameService = null;
        Closeable closeable = null;

        try (RpcAccessPoint rpcAccessPoint = ServiceSupport.load(RpcAccessPoint.class)) {
            // add service to service provider registry
            rpcAccessPoint.addServiceProvider(helloService, HelloService.class);
            log.info("Add service {} to service provider registry.", helloServiceName);

            // register to name service
            nameService = getNameService(rpcAccessPoint);
            registerToNameService(nameService, helloServiceName, rpcAccessPoint.getServerUri());

            // start server
            closeable = rpcAccessPoint.startServer();

            log.info("Server started, please input any key to stop server.");
            System.in.read();
            log.info("Server stopped.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (nameService != null) {
                nameService.close();
            }
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static NameService getNameService(RpcAccessPoint rpcAccessPoint) {
        URI nameServiceUri = getNameServiceUri(System.getProperty(TMP_DIR_ARG), NAME_SERVICE_FILE_NAME);
        log.info("Name service uri: {}.", nameServiceUri);
        return rpcAccessPoint.getNameService(nameServiceUri);
    }

    private static URI getNameServiceUri(String tmpDirPath, String nameServiceStorageFileName) {
        try {
            String sp = tmpDirPath + nameServiceStorageFileName;
            return new URI("file", null, sp, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static void registerToNameService(NameService nameService, String serviceName, URI serverUri) {
        assert nameService != null;
        log.info("Register service {} (server uri: {}) to name service.", serviceName, serverUri);
        try {
            nameService.registerService(serviceName, serverUri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
