package com.ishansong.diablo.plugin.plugins.monitor;

import com.ishansong.diablo.core.model.request.RequestDTO;

public interface RequestItemParser<T> {

    String getPath(T request);

    String getRemoteAddress(T request);

    String getRemoteUser();

    String getHeader(T request, String key);

    String getUrlParam(T request, String paramName);

    String getCookieValue(T request, String cookieName);

    String getHttpReferer(T request);

    String getUrl(T request);

    String getUri(T request);

    String getStatusCode(T request);

    String getClientIp(T request);

    String getUpstreamAddr(T request);

    String getUpstreamResponseTime(T request);

    String getHttpServerProtocal(T request);

    String getRequestTime(T request, RequestDTO requestDTO);
}
