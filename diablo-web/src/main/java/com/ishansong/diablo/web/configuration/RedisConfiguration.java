package com.ishansong.diablo.web.configuration;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.alicp.jetcache.redis.lettuce.JetCacheCodec;
import com.alicp.jetcache.redis.lettuce.RedisLettuceCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.masterslave.MasterSlave;
import io.lettuce.core.masterslave.StatefulRedisMasterSlaveConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableMethodCache(basePackages = "com.ishansong.diablo.web")
@EnableCreateCacheAnnotation
public class RedisConfiguration {

    @ApolloConfig("ISS.Redis")
    private Config redisConfig;

    @Bean
    public GlobalCacheConfig redisClient(@Value("${redis.sentinel.master.name:}") String master) {

        String sentinelPrefix = "redis.sentinel." + master;

        String nodes = redisConfig.getProperty(sentinelPrefix + ".nodes", "");
        String password = redisConfig.getProperty(sentinelPrefix + ".pwd", "");

        RedisURI redisURI = RedisURI.create("redis-sentinel://" + nodes + "/?sentinelMasterId=" + master);
        redisURI.setPassword(password);
        RedisClient redisClient = RedisClient.create();
        redisClient.setOptions(ClientOptions.builder().disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS).build());

        StatefulRedisMasterSlaveConnection connection = MasterSlave.connect(redisClient, new JetCacheCodec(), redisURI);
        connection.setReadFrom(ReadFrom.MASTER_PREFERRED);


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
