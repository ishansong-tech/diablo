package com.ishansong.diablo.plugin.plugins.monitor;

import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.model.access.AccessLog;
import com.ishansong.diablo.core.model.request.RequestDTO;
import com.ishansong.diablo.plugin.plugins.utils.AssertUtil;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Locale;

public class ParamParser<T> {

    private final RequestItemParser<T> requestItemParser;

    public ParamParser(RequestItemParser<T> requestItemParser) {
        AssertUtil.notNull(requestItemParser, "requestItemParser cannot be null");

        this.requestItemParser = requestItemParser;
    }
    FastDateFormat formatter = FastDateFormat.getInstance("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    public AccessLog parseParameterForAccessLog(T request, RequestDTO requestDTO){

        return AccessLog.builder()
                .date(formatter.format(requestDTO.getStartDateTime()))
                //如果有代理，这里不是真实客户端IP
                .remoteAddr(this.requestItemParser.getRemoteAddress(request))
                //暂时不支持此属性
                .remoteUser(this.requestItemParser.getRemoteUser())
                .requestMethod(requestDTO.getHttpMethod())
                .url(this.requestItemParser.getUrl(request))
                .uri(this.requestItemParser.getUri(request))
                .serverProtocol(this.requestItemParser.getHttpServerProtocal(request))
                .status(this.requestItemParser.getStatusCode(request))
                .bodyBytesSent("0.0")
                .httpReferrer(this.requestItemParser.getHttpReferer(request))
                .httpUserAgent(this.requestItemParser.getHeader(request,"User-Agent"))
                //真实ip
                .httpXForwardedFor(this.requestItemParser.getClientIp(request))
                .requestLength("0")
                .requestTime(this.requestItemParser.getRequestTime(request,requestDTO))
                .bytesSent("0.0")
                .requestBody(Constants.ACCESS_LOG_DEFAULT_PARAMS_STRING)
                .httpCookie(Constants.ACCESS_LOG_DEFAULT_PARAMS_STRING)
                .upstreamCacheStatus(Constants.ACCESS_LOG_DEFAULT_PARAMS_STRING)
                .upstreamAddr(this.requestItemParser.getUpstreamAddr(request))
                .upstreamResponseTime(this.requestItemParser.getUpstreamResponseTime(request))
                .build();
    }
}
