package com.ishansong.diablo.admin.listener.http;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.google.common.base.Throwables;
import com.ishansong.diablo.admin.listener.AbstractDataChangedListener;
import com.ishansong.diablo.admin.listener.CacheService;
import com.ishansong.diablo.admin.listener.ConfigDataCache;
import com.ishansong.diablo.admin.spring.SpringBeanUtils;
import com.ishansong.diablo.core.concurrent.DiabloThreadFactory;
import com.ishansong.diablo.core.constant.HttpConstants;
import com.ishansong.diablo.core.enums.ConfigGroupEnum;
import com.ishansong.diablo.core.enums.DataEventTypeEnum;
import com.ishansong.diablo.core.exception.DiabloException;
import com.ishansong.diablo.core.model.DiabloAdminResult;
import com.ishansong.diablo.core.model.dubbo.DubboResourceData;
import com.ishansong.diablo.core.model.plugin.PluginData;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.SelectorData;
import com.ishansong.diablo.core.utils.GsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class HttpLongPollingDataChangedListener extends AbstractDataChangedListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpLongPollingDataChangedListener.class);

    private static final String X_REAL_IP = "X-Real-IP";

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private static final String X_FORWARDED_FOR_SPLIT_SYMBOL = ",";

    private final BlockingQueue<LongPollingClient> clients;

    private final ScheduledExecutorService scheduler;

    public HttpLongPollingDataChangedListener() {
        this.clients = new ArrayBlockingQueue<>(1024);
        this.scheduler = new ScheduledThreadPoolExecutor(1,
                DiabloThreadFactory.create("long-polling", true));

        scheduler.scheduleWithFixedDelay(() -> {
            this.cacheService.updatePluginCache();
            this.cacheService.updateRuleCache();
            this.cacheService.updateSelectorCache();
            this.cacheService.updateDubboResourceCache();
        }, 300, 300, TimeUnit.SECONDS);

    }

    public void doLongPolling(final HttpServletRequest request, final HttpServletResponse response) {

        List<ConfigGroupEnum> changedGroup = compareMD5(request);
        String clientIp = getRemoteIp(request);

        if (CollectionUtils.isNotEmpty(changedGroup)) {
            LOGGER.info("send response with the changed group, ip={}, group={}", clientIp, changedGroup);

            this.generateResponse(response, changedGroup);
            return;
        }

        final AsyncContext asyncContext = request.startAsync();

        asyncContext.setTimeout(0L);

        scheduler.execute(new LongPollingClient(asyncContext, clientIp, HttpConstants.SERVER_MAX_HOLD_TIMEOUT));

        LOGGER.info("send response with the startAsync end, ip={}, group={}", clientIp, changedGroup);
    }

    @Override
    protected void afterPluginChanged(final List<PluginData> changed, final DataEventTypeEnum eventType, Long durationStart) {
        scheduler.execute(new DataChangeTask(ConfigGroupEnum.PLUGIN, durationStart));
    }

    @Override
    protected void afterRuleChanged(final List<RuleData> changed, final DataEventTypeEnum eventType, Long durationStart) {
        scheduler.execute(new DataChangeTask(ConfigGroupEnum.RULE, durationStart));
    }

    @Override
    protected void afterSelectorChanged(final List<SelectorData> changed, final DataEventTypeEnum eventType, Long durationStart) {
        scheduler.execute(new DataChangeTask(ConfigGroupEnum.SELECTOR, durationStart));
    }

    @Override
    protected void afterDubboResourceChanged(List<DubboResourceData> changed, DataEventTypeEnum eventType, Long durationStart) {
        scheduler.execute(new DataChangeTask(ConfigGroupEnum.DUBBO_MAPPING, durationStart));
    }

    private static List<ConfigGroupEnum> compareMD5(final HttpServletRequest request) {
        List<ConfigGroupEnum> changedGroup = new ArrayList<>(4);
        for (ConfigGroupEnum group : ConfigGroupEnum.values()) {
            // md5,lastModifyTime
            String[] params = StringUtils.split(request.getParameter(group.name()), ',');
            if (params == null || params.length != 2) {
                throw new DiabloException("group param invalid:" + request.getParameter(group.name()));
            }
            String clientMd5 = params[0];
            long clientModifyTime = NumberUtils.toLong(params[1]);
            ConfigDataCache serverCache = SpringBeanUtils.getInstance().getBean(CacheService.class).getConfigDataCacheService().get(group.name());

            if (serverCache == null) {
                LOGGER.info("HttpLongPollingDataChangedListener compareMD5 changed group, group={}, clientMd5={}, clientModifyTime={}"
                        , group.name(), clientMd5, clientModifyTime);

                changedGroup.add(group);
            } else if (!StringUtils.equals(clientMd5, serverCache.getMd5()) && clientModifyTime < serverCache.getLastModifyTime()) {
                LOGGER.info("HttpLongPollingDataChangedListener compareMD5 changed group, group={}, clientMd5={}, clientModifyTime={}, serverMd5={}, serverTime={}"
                        , group.name(), clientMd5, clientModifyTime, serverCache.getMd5(), serverCache.getLastModifyTime());

                changedGroup.add(group);
            }
        }

        LOGGER.info("HttpLongPollingDataChangedListener compareMD5 log, changedGroup={}", changedGroup);
        return changedGroup;
    }

    private void generateResponse(final HttpServletResponse response, final List<ConfigGroupEnum> changedGroups) {
        try {
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Cache-Control", "no-cache,no-store");
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(GsonUtils.getInstance().toJson(DiabloAdminResult.success("success", changedGroups)));
        } catch (Exception ex) {
            LOGGER.error("generateResponse Sending response failed.", ex);
        }
    }

    private static String getRemoteIp(final HttpServletRequest request) {
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (!StringUtils.isBlank(xForwardedFor)) {
            return xForwardedFor.split(X_FORWARDED_FOR_SPLIT_SYMBOL)[0].trim();
        }
        String header = request.getHeader(X_REAL_IP);
        return StringUtils.isBlank(header) ? request.getRemoteAddr() : header;
    }

    class DataChangeTask implements Runnable {

        private final ConfigGroupEnum groupKey;

        private final long changeTime = System.currentTimeMillis();

        private final Long durationStart;

        DataChangeTask(final ConfigGroupEnum groupKey, Long durationStart) {
            this.groupKey = groupKey;
            this.durationStart = durationStart;
        }

        @Override
        public void run() {
            try {
                if (CollectionUtils.isEmpty(clients)) {
                    reportedTransaction("客户端长连接列表为空,稍后重新连再发布");

                    return;
                }

                for (Iterator<LongPollingClient> iter = clients.iterator(); iter.hasNext(); ) {
                    LongPollingClient client = iter.next();
                    iter.remove();
                    client.sendResponse(Collections.singletonList(groupKey));

                    reportedTransaction(Transaction.SUCCESS);

                    LOGGER.info("send response with the changed group,ip={},group={},changeTime={}", client.ip, groupKey, changeTime);
                }
            } catch (Throwable e) {

                reportedTransaction(Throwables.getStackTraceAsString(e));
                LOGGER.error("data change error.", e);
            }
        }

        private void reportedTransaction(String status) {
            DefaultTransaction transaction = (DefaultTransaction) Cat.newTransaction("Notice_Sync", groupKey.name());

            if (durationStart != null && durationStart > 0) {
                transaction.setDurationStart(durationStart);
            }

            transaction.setStatus(status);
            transaction.complete();
        }
    }

    class LongPollingClient implements Runnable {

        private final AsyncContext asyncContext;

        private final String ip;

        private final long timeoutTime;

        private Future<?> asyncTimeoutFuture;

        LongPollingClient(final AsyncContext ac, final String ip, final long timeoutTime) {
            this.asyncContext = ac;
            this.ip = ip;
            this.timeoutTime = timeoutTime;
        }

        @Override
        public void run() {
            this.asyncTimeoutFuture = scheduler.schedule(() -> {
                clients.remove(LongPollingClient.this);
                List<ConfigGroupEnum> changedGroups = HttpLongPollingDataChangedListener.compareMD5((HttpServletRequest) asyncContext.getRequest());
                sendResponse(changedGroups);
            }, timeoutTime, TimeUnit.MILLISECONDS);
            clients.add(this);
        }

        void sendResponse(final List<ConfigGroupEnum> changedGroups) {
            if (null != asyncTimeoutFuture) {
                asyncTimeoutFuture.cancel(false);
            }
            LOGGER.info("sendResponse with the changed group,changedGroups={}", changedGroups);

            generateResponse((HttpServletResponse) asyncContext.getResponse(), changedGroups);
            asyncContext.complete();
        }
    }

}
