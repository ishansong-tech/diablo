package com.ishansong.diablo.web.configuration;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.alicp.jetcache.redis.lettuce.RedisLettuceCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
import com.ishansong.diablo.config.DiabloConfig;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableMethodCache(basePackages = "com.ishansong.diablo.web")
@EnableCreateCacheAnnotation
public class RedisConfiguration {

    @Autowired
    private DiabloConfig diabloConfig;

    @Bean
    public GlobalCacheConfig redisClient() {

        String nodes = diabloConfig.getWeb().getRedis().getNodes();

        RedisURI redisURI = RedisURI.create("redis://"+nodes);
        RedisClient redisClient = RedisClient.create(redisURI);
        redisClient.setOptions(ClientOptions.builder().disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS).build());

        StatefulRedisConnection connection = redisClient.connect();


        CacheBuilder redisLettuceCacheBuilder = RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
                                                                        .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                                                                        .valueEncoder(JavaValueEncoder.INSTANCE)
                                                                        .valueDecoder(JavaValueDecoder.INSTANCE)
                                                                        .connection(connection)
                                                                        .redisClient(redisClient);
        Map<String, CacheBuilder> remoteBuilders = new HashMap<String, CacheBuilder>() {{
            put(CacheConsts.DEFAULT_AREA, redisLettuceCacheBuilder);
        }};

        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.setConfigProvider(new SpringConfigProvider());
        globalCacheConfig.setRemoteCacheBuilders(remoteBuilders);
        globalCacheConfig.setStatIntervalMinutes(30);
        globalCacheConfig.setAreaInCacheName(false);

        return globalCacheConfig;
    }
}
