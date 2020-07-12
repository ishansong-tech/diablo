package com.ishansong.diablo.admin.utils;

import com.google.common.base.Throwables;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RestTemplateUtil {

    private static final Logger logger = LoggerFactory.getLogger(RestTemplateUtil.class);

    private static HttpClientConnectionManager poolingConnectionManager() {
        ConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http", plainSocketFactory)
                .register("https", sslSocketFactory).build();

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(registry);
        poolingConnectionManager.setMaxTotal(1000);
        poolingConnectionManager.setDefaultMaxPerRoute(500);

        SocketConfig socketConfig = SocketConfig.custom()
                //.setSoTimeout(500)       //接收数据的等待超时时间，单位ms
                .setSoKeepAlive(true)    //开启监视TCP连接是否有效
                .build();

        poolingConnectionManager.setDefaultSocketConfig(socketConfig);
        return poolingConnectionManager;
    }

    private static HttpClientBuilder httpClientBuilder() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setConnectionManager(poolingConnectionManager());
        httpClientBuilder.setRetryHandler(httpRequestRetryHandler());
        return httpClientBuilder;
    }

    private static HttpRequestRetryHandler httpRequestRetryHandler(){
        //请求失败时,进行请求重试
        HttpRequestRetryHandler handler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
                if (i > 0){
                    //重试超过0次,放弃请求
                    logger.error("httpRequestRetryHandler retry has more than "+i+" time, give up request,"+ Throwables.getStackTraceAsString(e));
                    return false;
                }
                if (e instanceof NoHttpResponseException){
                    //服务器没有响应,可能是服务器断开了连接,应该重试
                    logger.error("httpRequestRetryHandler receive no response from server, retry,"+ Throwables.getStackTraceAsString(e));
                    return true;
                }
                if (e instanceof SSLHandshakeException){
                    // SSL握手异常
                    logger.error("httpRequestRetryHandler SSL hand shake exception,"+ Throwables.getStackTraceAsString(e));
                    return false;
                }
                if (e instanceof InterruptedIOException){
                    //超时
                    logger.error("httpRequestRetryHandler InterruptedIOException,"+ Throwables.getStackTraceAsString(e));
                    return false;
                }
                if (e instanceof UnknownHostException){
                    // 服务器不可达
                    logger.error("httpRequestRetryHandler server host unknown,"+ Throwables.getStackTraceAsString(e));
                    return false;
                }
                if (e instanceof ConnectTimeoutException){
                    // 连接超时
                    logger.error("httpRequestRetryHandler ConnectTimeoutException,"+ Throwables.getStackTraceAsString(e));
                    return false;
                }
                if (e instanceof SSLException){
                    logger.error("httpRequestRetryHandler SSLException,"+ Throwables.getStackTraceAsString(e));
                    return false;
                }

                HttpClientContext context = HttpClientContext.adapt(httpContext);
                HttpRequest request = context.getRequest();
                if (!(request instanceof HttpEntityEnclosingRequest)){
                    //如果请求不是关闭连接的请求
                    return true;
                }
                return false;
            }
        };
        return handler;
    }

    private static HttpComponentsClientHttpRequestFactory getClientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(httpClientBuilder().build());
        clientHttpRequestFactory.setConnectionRequestTimeout(200);
        return clientHttpRequestFactory;
    }

    public static RestTemplate getRestTemplate(int connectTimeout,int readTimeout) {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory= getClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(connectTimeout);
        clientHttpRequestFactory.setReadTimeout(readTimeout);
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        List<HttpMessageConverter<?>> converterList = restTemplate.getMessageConverters();

        HttpMessageConverter<?> converterTarget = null;
        for (HttpMessageConverter<?> item : converterList) {
            if (StringHttpMessageConverter.class == item.getClass()) {
                converterTarget = item;
                break;
            }
        }
        if (null != converterTarget) {
            converterList.remove(converterTarget);
        }
        converterList.add(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }
}
