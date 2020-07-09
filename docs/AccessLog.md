# Access Log

## 背景

主要是针对分流插件，类似nginx中的路由转发，比如将[http://xxx.com/order] 开头的请求转发到一组主机中，线上的任意一个请求需要能追查到被转发到了哪台机器，就需要提供类似nginx的访问日志。

## 方案

在请求完成后，收集参数信息，批量/异步写入日志。

## 实现

### 日志数据模型

基本是参考nginx access log，但只是部分参数有效。这样做的好处是在统一日志分析系统中，运维只需要解析一种类型的请求日志即可，不需要单独做配置。

```java

public class AccessLog implements Serializable {

    private String date;
    @JsonProperty("remote_addr")
    private String remoteAddr;
    @JsonProperty("remote_user")
    private String remoteUser;
    @JsonProperty("request_method")
    private String requestMethod;
    private String url;
    private String uri;
    @JsonProperty("server_protocol")
    private String serverProtocol;
    private String status;
    @JsonProperty("body_bytes_sent")
    private String bodyBytesSent;
    @JsonProperty("http_referrer")
    private String httpReferrer;
    @JsonProperty("http_user_agent")
    private String httpUserAgent;
    @JsonProperty("http_x_forwarded_for")
    private String httpXForwardedFor;
    @JsonProperty("request_length")
    private String requestLength;
    @JsonProperty("request_time")
    private String requestTime;
    @JsonProperty("bytes_sent")
    private String bytesSent;
    @JsonProperty("request_body")
    private String requestBody;
    @JsonProperty("http_cookie")
    private String httpCookie;
    @JsonProperty("upstream_cache_status")
    private String upstreamCacheStatus;
    @JsonProperty("upstream_addr")
    private String upstreamAddr;
    @JsonProperty("upstream_response_time")
    private String upstreamResponseTime;


}


```

### 日志工具

原想法是构建一个日志事件，批量去写日志。

> 此方案上线后，因为请求日志量比较大，1台机器1天会出现大概1次cpu load值过高的现象（实际可能比这个数多，因为我们监控系统的数据采集并不是秒级的），最大值接受cpu core。尝试过很多方案，比如log4j的异步日志，还有ChannelFile方式等。规律就是当日志量下降后此现象消失，目前具体原因依然未解决，线上为了避免风险，关闭了日志。

```java

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

```