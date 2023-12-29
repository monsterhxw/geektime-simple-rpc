package com.github.monsterhxw.rpc.client;

import com.github.monsterhxw.rpc.api.NameService;
import com.github.monsterhxw.rpc.api.RpcAccessPoint;
import com.github.monsterhxw.rpc.api.spi.ServiceSupport;
import com.github.monsterhxw.rpc.hello.service.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;

/**
 * @author huangxuewei
 * @since 2023/12/29
 */
public class ClientBootstrap {

    private static final Logger log = LoggerFactory.getLogger(ClientBootstrap.class);

    private static final String TMP_DIR_ARG = "java.io.tmpdir";
    private static final String NAME_SERVICE_FILE_NAME = "simple_rpc_name_service.data";

    public static void main(String[] args) {
        String serviceName = HelloService.class.getCanonicalName();
        NameService nameService = null;

        try (RpcAccessPoint rpcAccessPoint = ServiceSupport.load(RpcAccessPoint.class)) {
            // get name service
            nameService = getNameService(rpcAccessPoint);
            URI uri = nameService.lookupService(serviceName);
            assert uri != null;
            log.info("lookup {} uri: {}.", serviceName, uri);

            // get service stub
            HelloService helloService = rpcAccessPoint.getRemoteService(uri, HelloService.class);
            log.info("Get service stub: {}.", helloService);

            // remote call helloService#hello
            String arg = "World!";
            String res = helloService.hello(arg);
            log.info("Remote call helloService#hello(\"{}\") return: {}.", arg, res);

            // remote call helloService#hello
            arg = "Huang Xuewei";
            res = helloService.hello(arg);
            log.info("Remote call helloService#hello(\"{}\") return: {}.", arg, res);

            // remote call helloService#hello
            arg = "Your Argument Here";
            res = helloService.hello(arg);
            log.info("Remote call helloService#hello(\"{}\") return: {}.", arg, res);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (nameService != null) {
                nameService.close();
            }
        }
    }

    private static NameService getNameService(RpcAccessPoint rpcAccessPoint) {
        URI nameServiceUri = getNameServiceUri(System.getProperty(TMP_DIR_ARG), NAME_SERVICE_FILE_NAME);
        log.info("Name service uri: {}.", nameServiceUri);
        return rpcAccessPoint.getNameService(nameServiceUri);
    }

    private static URI getNameServiceUri(String tmpDir, String nameServiceFileName) {
        return URI.create("file://" + tmpDir + File.separator + nameServiceFileName);
    }
}
