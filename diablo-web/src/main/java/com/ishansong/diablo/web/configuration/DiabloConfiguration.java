package com.ishansong.diablo.web.configuration;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.SerialPolicy;
import com.ishansong.diablo.cache.LocalCacheManager;
import com.ishansong.diablo.cache.UpstreamCacheManager;
import com.ishansong.diablo.plugin.limiter.LimiterPlugin;
import com.ishansong.diablo.plugin.plugins.DiabloPlugin;
import com.ishansong.diablo.plugin.plugins.divide.DividePlugin;
import com.ishansong.diablo.plugin.plugins.dubbo.DubboPlugin;
import com.ishansong.diablo.plugin.plugins.dubbo.DubboProxyService;
import com.ishansong.diablo.plugin.plugins.monitor.MonitorPlugin;
import com.ishansong.diablo.plugin.plugins.response.ResponsePlugin;
import com.ishansong.diablo.plugin.plugins.token.TokenAuthSDKService;
import com.ishansong.diablo.plugin.plugins.token.TokenPlugin;
import com.ishansong.diablo.plugin.plugins.waf.WafPlugin;
import com.ishansong.diablo.web.filter.BodyWebFilter;
import com.ishansong.diablo.web.filter.ParamWebFilter;
import com.ishansong.diablo.web.handler.DiabloWebHandler;
import com.ishansong.diablo.web.handler.FailbackReporter;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.WriteBufferWaterMark;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.filter.reactive.HiddenHttpMethodFilter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.netty.http.HttpResources;
import reactor.netty.http.client.HttpClient;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@Import(value = {CacheConfiguration.class,GatewayConfiguration.class})
public class DiabloConfiguration {

    private final LocalCacheManager localCacheManager;

    private final UpstreamCacheManager upstreamCacheManager;

    public DiabloConfiguration(final LocalCacheManager localCacheManager,
                               final UpstreamCacheManager upstreamCacheManager) {
        this.localCacheManager = localCacheManager;
        this.upstreamCacheManager = upstreamCacheManager;
    }

    private static final int DEFAULT_LOW_WATER_MARK = 32 * 1024;
    private static final int DEFAULT_HIGH_WATER_MARK = 1024 * 1024;

    @CreateCache(expire = 30 * 60, name = "token_UserIdGatewayCache", cacheType = CacheType.REMOTE, serialPolicy = SerialPolicy.KRYO)
    private Cache<String, Object> tokenUserIdGatewayCache;

    @Bean
    public DiabloPlugin dividePlugin() {

        WebClient webClient = WebClient.builder()
                                       .clientConnector(new ReactorClientHttpConnector(
                                               HttpClient.create(HttpResources.get())
                                                         .tcpConfiguration(t -> t.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3_000)
                                                                                 .option(ChannelOption.SO_KEEPALIVE, true)
                                                                                 .option(ChannelOption.TCP_NODELAY, true)
                                                                                 .option(ChannelOption.SO_REUSEADDR, true)
                                                                                 .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                                                                                 .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(DEFAULT_LOW_WATER_MARK, DEFAULT_HIGH_WATER_MARK))
                                                                 // .doOnDisconnected(DisposableChannel::disposeNow)
                                                         )
                                       ))
                                       .build();

        return new DividePlugin(localCacheManager, upstreamCacheManager, webClient);
    }

    @Bean
    public DiabloPlugin responsePlugin() {
        return new ResponsePlugin();
    }

    @Bean
    public DiabloPlugin breakerPlugin() {
        return new MonitorPlugin(localCacheManager);
    }

    @Bean
    public DiabloPlugin limiterPlugin() {
        return new LimiterPlugin(localCacheManager);
    }

    @Bean
    public DiabloPlugin wafPlugin() {
        return new WafPlugin(localCacheManager);
    }

    @Bean
    public DiabloPlugin dubboPlugin() {

        return new DubboPlugin(localCacheManager, new DubboProxyService());
    }

    @Bean
    public DiabloPlugin tokenPlugin() {
        return new TokenPlugin(localCacheManager, new TokenAuthSDKService(tokenUserIdGatewayCache), new DubboProxyService());
    }


    @Bean("webHandler")
    public DiabloWebHandler diabloWebHandler(final List<DiabloPlugin> plugins, final FailbackReporter failbackReporter) {
        final List<DiabloPlugin> diabloPlugins = plugins.stream()
                                                        .sorted((m, n) -> {
                                                            if (m.pluginType().equals(n.pluginType())) {
                                                                return m.getOrder() - n.getOrder();
                                                            } else {
                                                                return m.pluginType().getName().compareTo(n.pluginType().getName());
                                                            }
                                                        }).collect(Collectors.toList());
        return new DiabloWebHandler(diabloPlugins, failbackReporter);
    }

    @Bean
    @Order(1)
    public WebFilter paramWebFilter() {
        return new ParamWebFilter();
    }

    @Bean
    @Order(-1)
    public BodyWebFilter bodyWebFilter() {

        return new BodyWebFilter();
    }

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
                return chain.filter(exchange);
            }
        };
    }
}
