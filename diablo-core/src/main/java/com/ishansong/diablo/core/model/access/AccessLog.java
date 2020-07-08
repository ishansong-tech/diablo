package com.ishansong.diablo.core.model.access;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessLog implements Serializable {

    //请求时间
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
