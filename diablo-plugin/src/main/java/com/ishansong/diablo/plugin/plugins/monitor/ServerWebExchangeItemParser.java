package com.ishansong.diablo.plugin.plugins.monitor;

import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.model.request.RequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;

public class ServerWebExchangeItemParser implements RequestItemParser<ServerWebExchange> {

    private static final Logger logger = LoggerFactory.getLogger(ServerWebExchangeItemParser.class);

    @Override
    public String getPath(ServerWebExchange exchange) {
        return exchange.getRequest().getPath().value();
    }

    @Override
    public String getRemoteAddress(ServerWebExchange exchange) {

        String remote = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                                .map(InetSocketAddress::getAddress)
                                .map(InetAddress::getHostAddress)
                                .orElse("-");

        return remote;
    }

    @Override
    public String getRemoteUser() {
        return Constants.ACCESS_LOG_DEFAULT_PARAMS_STRING;
    }

    @Override
    public String getHeader(ServerWebExchange exchange, String key) {
        return exchange.getRequest().getHeaders().getFirst(key);
    }

    @Override
    public String getUrlParam(ServerWebExchange exchange, String paramName) {
        return exchange.getRequest().getQueryParams().getFirst(paramName);
    }

    @Override
    public String getCookieValue(ServerWebExchange exchange, String cookieName) {
        return Optional.ofNullable(exchange.getRequest().getCookies().getFirst(cookieName))
                       .map(HttpCookie::getValue)
                       .orElse(null);
    }

    @Override
    public String getHttpReferer(ServerWebExchange request) {
        if (request.getRequest().getHeaders().containsKey("Referer")) {
            return this.getHeader(request, "Referer");
        }
        return Constants.ACCESS_LOG_DEFAULT_PARAMS_STRING;
    }

    @Override
    public String getUrl(ServerWebExchange request) {
        return request.getRequest().getURI().getScheme() + "://" + request.getRequest().getURI().getAuthority();
    }

    @Override
    public String getUri(ServerWebExchange request) {
        return request.getRequest().getURI().getPath() + (StringUtils.isEmpty(request.getRequest().getURI().getQuery()) ? "" : "?" + request.getRequest().getURI().getQuery());
    }

    @Override
    public String getStatusCode(ServerWebExchange request) {
        return null != request.getResponse().getStatusCode() ?
                request.getResponse().getStatusCode().value() + "" : "999";

    }

    @Override
    public String getClientIp(ServerWebExchange request) {

        String ip = this.getHeader(request, "x-source-id");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = this.getHeader(request, "x-forwarded-for");
            logger.debug("[getIpAddr]: x-forwarded-for ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = this.getHeader(request, "X-Forwarded-For");
            logger.debug("[getIpAddr]: X-Forwarded-For ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = this.getHeader(request, "Proxy-Client-IP");
            logger.debug("[getIpAddr]:Proxy-Client-IP: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = this.getHeader(request, "WL-Proxy-Client-IP");
            logger.debug("[getIpAddr]: WL-Proxy-Client-IP ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = this.getRemoteAddress(request);
            logger.debug("[getIpAddr]: getRemoteAddr ip: " + ip);
        }

        return ip;
    }

    @Override
    public String getUpstreamAddr(ServerWebExchange request) {
        return request.getAttributes().containsKey(Constants.CLIENT_RESPONSE_UPSTREAM_ADDR) ?
                request.getAttributes().get(Constants.CLIENT_RESPONSE_UPSTREAM_ADDR) + "" : Constants.ACCESS_LOG_DEFAULT_PARAMS_STRING;
    }

    @Override
    public String getUpstreamResponseTime(ServerWebExchange request) {
        if (request.getAttributes().containsKey(Constants.CLIENT_RESPONSE_UPSTREAM_REQUEST_TIME)
                && request.getAttributes().containsKey(Constants.CLIENT_RESPONSE_UPSTREAM_RESPONSE_TIME)) {
            Long responseTime = Long.parseLong(request.getAttributes().get(Constants.CLIENT_RESPONSE_UPSTREAM_RESPONSE_TIME).toString());
            Long requestTime = Long.parseLong(request.getAttributes().get(Constants.CLIENT_RESPONSE_UPSTREAM_REQUEST_TIME).toString());
            return this.convertMillisSecond2Second(responseTime - requestTime);
        }
        return Constants.ACCESS_LOG_DEFAULT_PARAMS_STRING;
    }

    @Override
    public String getHttpServerProtocal(ServerWebExchange request) {
        //TODO
        return Constants.ACCESS_LOG_DEFAULT_PARAMS_STRING;
    }

    @Override
    public String getRequestTime(ServerWebExchange request, RequestDTO requestDTO) {
        return this.convertMillisSecond2Second(System.currentTimeMillis() - requestDTO.getStartDateTime());
    }

    private String convertMillisSecond2Second(Long millisSecond) {
        return String.format("%.3f", (double) millisSecond / 1000);
    }
}
