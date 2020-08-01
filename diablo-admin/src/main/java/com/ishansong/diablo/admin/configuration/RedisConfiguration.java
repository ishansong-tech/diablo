package com.ishansong.diablo.admin.configuration;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.redis.lettuce.JetCacheCodec;
import com.alicp.jetcache.redis.lettuce.RedisLettuceCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
import com.ishansong.diablo.config.DiabloConfig;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.masterslave.MasterSlave;
import io.lettuce.core.masterslave.StatefulRedisMasterSlaveConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@AutoConfigureBefore(DataSyncConfiguration.class)
@EnableMethodCache(basePackages = "com.ishansong.diablo.admin")
@EnableCreateCacheAnnotation
public class RedisConfiguration {

    @Autowired
    private DiabloConfig diabloConfig;

    @Bean
    public GlobalCacheConfig redisClient( ) {

        String master=diabloConfig.getAdmin().getRedis().getMaster();

        String nodes = diabloConfig.getAdmin().getRedis().getNodes();
        String password = diabloConfig.getAdmin().getRedis().getPwd();

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

        Map localBuilders = new HashMap();
        EmbeddedCacheBuilder localBuilder = LinkedHashMapCacheBuilder
                .createLinkedHashMapCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE);
        localBuilders.put(CacheConsts.DEFAULT_AREA, localBuilder);

        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.setConfigProvider(new SpringConfigProvider());
        globalCacheConfig.setRemoteCacheBuilders(remoteBuilders);
        globalCacheConfig.setLocalCacheBuilders(localBuilders);
        globalCacheConfig.setStatIntervalMinutes(30);
        globalCacheConfig.setAreaInCacheName(false);

        return globalCacheConfig;
    }
}
