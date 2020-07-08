package com.ishansong.diablo.web.configuration;

import com.ishansong.diablo.config.DiabloConfig;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import reactor.netty.http.server.HttpServer;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

//@Configuration
public class DiabloNettyWebServerFactory {

    private static final Logger logger= LoggerFactory.getLogger(DiabloNettyWebServerFactory.class);

    @ConfigurationProperties(prefix = "diablo.netty")
    @Bean
    public DiabloConfig.NettyConfig nettyConfig() {
        return new DiabloConfig.NettyConfig();
    }

    @Bean
    public NettyReactiveWebServerFactory nettyReactiveWebServerFactory() {
        NettyReactiveWebServerFactory webServerFactory = new NettyReactiveWebServerFactory();
        webServerFactory.addServerCustomizers(new EventLoopNettyCustomizer(this.nettyConfig()));
        return webServerFactory;
    }

    private static class EventLoopNettyCustomizer implements NettyServerCustomizer {

        public static final String OS_NAME = System.getProperty("os.name");

        private static boolean isLinuxPlatform = false;

        private DiabloConfig.NettyConfig nettyConfig;

        private final EventLoopGroup eventLoopGroupSelector;

        private final EventLoopGroup eventLoopGroupBoss;

        static {
            if (OS_NAME != null && OS_NAME.toLowerCase().contains("linux")) {
                isLinuxPlatform = true;
            }

        }

        public static boolean isLinuxPlatform() {
            return isLinuxPlatform;
        }

        public EventLoopNettyCustomizer(DiabloConfig.NettyConfig nettyConfig){
            this.nettyConfig=nettyConfig;

            if (useEpoll()) {
                this.eventLoopGroupBoss = new EpollEventLoopGroup(this.nettyConfig.getServerBossThreads(), new ThreadFactory() {
                    private AtomicInteger threadIndex = new AtomicInteger(0);
                    private int threadTotal = nettyConfig.getServerBossThreads();

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, String.format("NettyEPOLLBoss_%d_%d",threadTotal, this.threadIndex.incrementAndGet()));
                    }
                });

                this.eventLoopGroupSelector = new EpollEventLoopGroup(this.nettyConfig.getServerSelectorThreads(), new ThreadFactory() {
                    private AtomicInteger threadIndex = new AtomicInteger(0);
                    private int threadTotal = nettyConfig.getServerSelectorThreads();

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, String.format("NettyServerEPOLLSelector_%d_%d", threadTotal, this.threadIndex.incrementAndGet()));
                    }
                });
            } else {
                this.eventLoopGroupBoss = new NioEventLoopGroup(this.nettyConfig.getServerBossThreads(), new ThreadFactory() {
                    private AtomicInteger threadIndex = new AtomicInteger(0);
                    private int threadTotal = nettyConfig.getServerBossThreads();

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, String.format("NettyNIOBoss_%d_%d",threadTotal, this.threadIndex.incrementAndGet()));
                    }
                });

                this.eventLoopGroupSelector = new NioEventLoopGroup(this.nettyConfig.getServerSelectorThreads(), new ThreadFactory() {
                    private AtomicInteger threadIndex = new AtomicInteger(0);
                    private int threadTotal = nettyConfig.getServerSelectorThreads();

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, String.format("NettyServerNIOSelector_%d_%d", threadTotal, this.threadIndex.incrementAndGet()));
                    }
                });
            }
        }

        @Override
        public HttpServer apply(HttpServer httpServer) {

            return httpServer
                    .tcpConfiguration(tcpServer -> tcpServer.bootstrap(
                            serverBootstrap -> serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupSelector)
                                    .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                                    //.childOption(ChannelOption.SO_SNDBUF, this.nettyConfig.getServerSocketSndBufSize())
                                    //.childOption(ChannelOption.SO_RCVBUF, this.nettyConfig.getServerSocketRcvBufSize())
                    ));
        }

        private boolean useEpoll() {
            logger.info("useEpoll:isLinuxPlatform:{},Epoll.isAvailable:{}",
                    isLinuxPlatform(),
                    Epoll.isAvailable()
                    );
            return isLinuxPlatform()
                    && Epoll.isAvailable();
        }
    }
}
