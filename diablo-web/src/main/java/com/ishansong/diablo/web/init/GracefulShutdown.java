package com.ishansong.diablo.web.init;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.stereotype.Component;
import reactor.netty.http.HttpResources;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpResources;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.time.Duration;

@Slf4j
@Component
public class GracefulShutdown {

    @Autowired
    ReactorResourceFactory reactorResourceFactory;

    private ConnectionProvider connectionProvider;

    private LoopResources loopResources;

    @PostConstruct
    public void init() {

        try {
            Field defaultLoops = TcpResources.class.getDeclaredField("defaultLoops");
            Field defaultProvider = TcpResources.class.getDeclaredField("defaultProvider");
            defaultLoops.setAccessible(true);
            defaultProvider.setAccessible(true);

            loopResources = (LoopResources) defaultLoops.get(reactorResourceFactory.getLoopResources());
            connectionProvider = (ConnectionProvider) defaultProvider.get(reactorResourceFactory.getConnectionProvider());
            defaultLoops.setAccessible(false);
            defaultProvider.setAccessible(false);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {

                long started = System.currentTimeMillis();

                log.info("GracefulShutdown loopResources dispose block to 45s before real shutdown start");

                HttpResources.disposeLoopsAndConnectionsLater().block(Duration.ofSeconds(1));

                if (connectionProvider != null) {
                    connectionProvider.disposeLater().block(Duration.ofSeconds(1));
                }
                try {
                    loopResources.disposeLater().block(Duration.ofSeconds(1));
                } catch (Exception e) {
                    log.info("GracefulShutdown loopResources dispose block failed, cause:{}", Throwables.getStackTraceAsString(e));
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ingore
                }

                log.info("GracefulShutdown loopResources dispose block 45s shutdown end, elased:{}", System.currentTimeMillis() - started);
            }));

        } catch (Exception e) {
            log.error("GracefulShutdown shutdown failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }
}
