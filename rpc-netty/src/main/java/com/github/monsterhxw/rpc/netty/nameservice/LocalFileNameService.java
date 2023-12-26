package com.github.monsterhxw.rpc.netty.nameservice;

import com.github.monsterhxw.rpc.api.NameService;
import com.github.monsterhxw.rpc.netty.serialize.SerializeSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public class LocalFileNameService implements NameService {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileNameService.class);

    private static final Collection<String> SUPPORTED_SCHEMES = Collections.singleton("file");

    private File file;

    @Override
    public Collection<String> supportedSchemes() {
        return SUPPORTED_SCHEMES;
    }

    @Override
    public void connect(URI nameServiceUri) {
        if (SUPPORTED_SCHEMES.contains(nameServiceUri.getScheme())) {
            file = new File(nameServiceUri);
        } else {
            throw new IllegalArgumentException("Unsupported scheme: " + nameServiceUri.getScheme());
        }
    }

    @Override
    public void registerService(String serviceName, URI uri) throws IOException {
        LOG.info("Register service: {} to name service: {}.", serviceName, uri);
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel fileChannel = raf.getChannel()) {
            FileLock lock = fileChannel.lock();
            try {
                long fileLen = raf.length();
                Metadata metadata;
                byte[] bytes;
                if (fileLen > 0L) {
                    bytes = new byte[(int) raf.length()];
                    ByteBuffer buffer = ByteBuffer.wrap(bytes);
                    while (buffer.hasRemaining()) {
                        fileChannel.read(buffer);
                    }
                    metadata = SerializeSupport.deserialize(bytes);
                } else {
                    metadata = new Metadata();
                }
                List<URI> uris = metadata.computeIfAbsent(serviceName, __ -> new ArrayList<>());
                if (!uris.contains(uri)) {
                    uris.add(uri);
                }
                LOG.info("Metadata: {}.", metadata);

                bytes = SerializeSupport.serialize(metadata);
                fileChannel.truncate(bytes.length);
                fileChannel.position(0L);
                fileChannel.write(ByteBuffer.wrap(bytes));
                fileChannel.force(true);
            } finally {
                lock.release();
            }
        }
    }

    @Override
    public URI lookupService(String serviceName) throws IOException {
        Metadata metadata;
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel fileChannel = raf.getChannel()) {
            FileLock lock = fileChannel.lock();
            try {
                byte[] bytes = new byte[(int) raf.length()];
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) {
                    fileChannel.read(buffer);
                }
                metadata = bytes.length == 0 ? new Metadata() : SerializeSupport.deserialize(bytes);
                LOG.info("Metadata: {}.", metadata);
            } finally {
                lock.release();
            }
        }
        List<URI> uris = metadata.get(serviceName);
        if (null == uris || uris.isEmpty()) {
            return null;
        }
        return uris.get(ThreadLocalRandom.current().nextInt(uris.size()));
    }

    @Override
    public void close() {
        if (file != null) {
            file.deleteOnExit();
        }
        LOG.info("Name service closed.");
    }
}
