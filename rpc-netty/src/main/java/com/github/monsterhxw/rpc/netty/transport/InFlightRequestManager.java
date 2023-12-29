package com.github.monsterhxw.rpc.netty.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author huangxuewei
 * @since 2023/12/30
 */
public class InFlightRequestManager implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(InFlightRequestManager.class);

    private final static long TIMEOUT_MILLIS = 10_000L;

    private final static long LOCK_TIMEOUT_MILLIS = 5_000L;

    private final ConcurrentMap<Integer /* requestId */, ResponseFuture> responseTable = new ConcurrentHashMap<>(256);
    private final Semaphore semaphore = new Semaphore(20);
    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledFuture<?> scheduledFuture = scheduledExecutor
            .scheduleAtFixedRate(this::scanAndRemoveTimeoutResponseFuture, TIMEOUT_MILLIS, TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

    @Override
    public void close() throws IOException {
        this.scheduledFuture.cancel(true);
        this.scheduledExecutor.shutdown();
    }

    public void putResponseFuture(ResponseFuture responseFuture) throws InterruptedException, TimeoutException {
        if (this.semaphore.tryAcquire(this.LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            this.responseTable.put(responseFuture.getRequestId(), responseFuture);
        } else {
            throw new TimeoutException("Failed to acquire semaphore, requestId: " + responseFuture.getRequestId());
        }
    }

    public ResponseFuture removeResponseFuture(int requestId) {
        ResponseFuture removeFuture = this.responseTable.remove(requestId);
        if (removeFuture != null) {
            this.semaphore.release();
        }
        return removeFuture;
    }

    private void scanAndRemoveTimeoutResponseFuture() {
        Iterator<Map.Entry<Integer, ResponseFuture>> it = this.responseTable.entrySet().iterator();
        while (it.hasNext()) {
            ResponseFuture respFuture = it.next().getValue();

            if ((respFuture.getBeginTimestamp() + respFuture.getTimeoutMillis() + 1_000L) <= System.currentTimeMillis()) {
                this.semaphore.release();
                it.remove();
                log.warn("Remove timeout response future, requestId: {}", respFuture.getRequestId());
            }
        }
    }
}
