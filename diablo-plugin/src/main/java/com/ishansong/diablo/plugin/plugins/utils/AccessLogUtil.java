package com.ishansong.diablo.plugin.plugins.utils;

import com.google.common.base.Throwables;
import com.ishansong.diablo.core.concurrent.DiabloThreadFactory;
import com.ishansong.diablo.core.model.access.AccessLog;
import com.ishansong.diablo.core.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.WorkQueueProcessor;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by jiangmin on 2019/12/3.
 * 批量写日志
 */
@Component
public class AccessLogUtil implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(AccessLogUtil.class);

    private static final Logger accessLogger=LoggerFactory.getLogger("accesslogLogger");

    private static final WorkQueueProcessor<AccessLog> PROCESSOR = WorkQueueProcessor.create();

    private static final Executor FLUSH_LOG_EXECUTOR = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), DiabloThreadFactory.create("flush-accesslog-executor", false), new ThreadPoolExecutor.AbortPolicy());

    @Override
    public void afterPropertiesSet() throws Exception {

        logger.info("1.1.2.log18");
        Flux<List<AccessLog>> flux = PROCESSOR.window(32).flatMap(accessLogFlux -> {
            return accessLogFlux.reduce(new ArrayList<>(),(objects, accessLog) -> {
                objects.add(accessLog);
                return objects;
            });
        });
        flux.subscribe(accessLogs -> {
            FLUSH_LOG_EXECUTOR.execute(() -> {
                try {
                    StringBuilder stringBuilder = new StringBuilder();

                    if (CollectionUtils.isEmpty(accessLogs)) {
                        return;
                    }
                    int accessLogSize = accessLogs.size();
                    for (int i = 0; i < accessLogSize; i++) {
                        stringBuilder.append(JsonUtils.toJson(accessLogs.get(i)));
                        if (i != accessLogSize - 1) {
                            stringBuilder.append("\n");
                        }
                    }
                    accessLogger.info(stringBuilder.toString());
                } catch (Exception ex) {
                    logger.error("AccessLogUtil.writelog error:{}", Throwables.getStackTraceAsString(ex));
                } finally {
                    accessLogs.clear();
                }
            });
        });
    }

    public static void postAccessLogEvent(AccessLog accessLog){
        PROCESSOR.onNext(accessLog);
    }
}
