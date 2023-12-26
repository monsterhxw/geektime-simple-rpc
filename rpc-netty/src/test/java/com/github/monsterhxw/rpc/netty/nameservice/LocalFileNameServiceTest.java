package com.github.monsterhxw.rpc.netty.nameservice;

import com.github.monsterhxw.rpc.api.NameService;
import com.github.monsterhxw.rpc.api.spi.ServiceSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
class LocalFileNameServiceTest {

    private LocalFileNameService localFileNameService;

    private static final String TMP_DIR_ARG = "java.io.tmpdir";

    private static final String NAME_SERVICE_FILE_NAME = "simple_rpc_name_service.data";

    private final URI fileUri = URI.create("file://" + System.getProperty(TMP_DIR_ARG) + "/" + NAME_SERVICE_FILE_NAME);

    @BeforeEach
    void setUp() {
        Collection<NameService> nameServices = ServiceSupport.loadAll(NameService.class);
        for (NameService nameService : nameServices) {
            if (nameService instanceof LocalFileNameService) {
                localFileNameService = (LocalFileNameService) nameService;
                break;
            }
        }
        assertNotNull(localFileNameService);

        localFileNameService.connect(fileUri);
    }

    @AfterEach
    void tearDown() {
        localFileNameService.close();
        localFileNameService = null;
    }

    @Test
    void supportedSchemes() {
        Collection<String> supportedSchemes = localFileNameService.supportedSchemes();
        assertTrue(supportedSchemes.contains("file"));
    }

    @Test
    void registerService() throws IOException {
        URI uri = URI.create("rpc://127.0.0.1:19999");
        String serviceName = "testService";

        localFileNameService.registerService(serviceName, uri);

        URI lookupUri = localFileNameService.lookupService(serviceName);

        assertEquals(uri, lookupUri);
    }

    @Test
    void lookupService() throws IOException {
        URI uri = localFileNameService.lookupService("testService");
        assertNull(uri);
    }
}