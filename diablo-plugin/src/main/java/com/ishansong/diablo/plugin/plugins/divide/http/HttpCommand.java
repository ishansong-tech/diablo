package com.ishansong.diablo.plugin.plugins.divide.http;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.enums.HttpMethodEnum;
import com.ishansong.diablo.core.model.request.RequestDTO;
import com.ishansong.diablo.core.model.selector.DivideUpstream;
import com.ishansong.diablo.plugin.plugins.DiabloPluginChain;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.AbstractServerHttpResponse;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@Slf4j(topic = "requestTraceLogger")
public class HttpCommand {

    private final ServerWebExchange exchange;

    private final DiabloPluginChain chain;

    private final RequestDTO requestDTO;

    private final DivideUpstream divideUpstream;

    private final Integer timeout;

    private final WebClient webClient;

    private String traceId = "";

    private final long requestStartTime = System.currentTimeMillis();

    public HttpCommand(
            final ServerWebExchange exchange,
            final DiabloPluginChain chain,
            final RequestDTO requestDTO,
            final DivideUpstream divideUpstream,
            final WebClient webClient,
            final Integer timeout) {
        this.exchange = exchange;
        this.chain = chain;
        this.requestDTO = requestDTO;
        this.divideUpstream = divideUpstream;
        this.webClient = webClient;
        this.timeout = timeout;
    }

    public Mono<Void> doHttpInvoke() {

        URI uri = buildRealURL(divideUpstream, exchange);
        traceId = exchange.getAttribute(Constants.CLIENT_RESPONSE_TRACE_ID);
        if (uri == null) {
            log.warn("HttpCommand.doNext real url is null, traceId: {}, uri: {}", traceId, exchange.getRequest().getURI().getPath());
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return chain.execute(exchange).then(Mono.defer(() -> Mono.empty()));
        }
        // 后续有时间再加 todo 没有清除掉
        // IssRpcContext.commitParams(IssRpcContextParamKey.TRACE_ID, traceId);

        if (requestDTO.getHttpMethod().equals(HttpMethodEnum.GET.getName())) {

            return webClient.get().uri(f -> uri)
                            .headers(httpHeaders -> {
                                httpHeaders.add(Constants.TRACE_ID, traceId);
                                httpHeaders.addAll(exchange.getRequest().getHeaders());
                            })
                            .exchange()
                            // 默认doOnError异常会传递
                            .doOnError(e -> log.error("HttpCommand.doHttpInvoke Failed to webClient get execute, traceId: {}, uri: {}, cause:{}", traceId, uri, Throwables.getStackTraceAsString(e)))
                            .timeout(Duration.ofMillis(timeout))
                            .flatMap(this::doNext);
        } else if (requestDTO.getHttpMethod().equals(HttpMethodEnum.POST.getName())) {

            return webClient.post().uri(f -> uri)
                            .headers(httpHeaders -> {
                                httpHeaders.add(Constants.TRACE_ID, traceId);
                                httpHeaders.addAll(exchange.getRequest().getHeaders());
                            })
                            .contentType(buildMediaType())
                            .body(BodyInserters.fromDataBuffers(exchange.getRequest().getBody()))
                            .exchange()
                            .doOnError(e -> log.error("HttpCommand.doHttpInvoke Failed to webClient post execute, traceId: {}, uri: {}, cause:{}", traceId, uri, Throwables.getStackTraceAsString(e)))
                            .timeout(Duration.ofMillis(timeout))
                            .flatMap(this::doNext);
        } else if (requestDTO.getHttpMethod().equals(HttpMethodEnum.OPTIONS.getName())) {
            return webClient.options().uri(f -> uri)
                            .headers(httpHeaders -> {
                                httpHeaders.add(Constants.TRACE_ID, traceId);
                                httpHeaders.addAll(exchange.getRequest().getHeaders());
                            })
                            .exchange()
                            .doOnError(e -> log.error("HttpCommand.doHttpInvoke Failed to webClient options execute, traceId: {}, uri: {}, cause:{}", traceId, uri, Throwables.getStackTraceAsString(e)))
                            .timeout(Duration.ofMillis(timeout))
                            .flatMap(this::doNext);
        } else if (requestDTO.getHttpMethod().equals(HttpMethodEnum.HEAD.getName())) {
            return webClient.head().uri(f -> uri)
                            .headers(httpHeaders -> {
                                httpHeaders.add(Constants.TRACE_ID, traceId);
                                httpHeaders.addAll(exchange.getRequest().getHeaders());
                            })
                            .exchange()
                            .doOnError(e -> log.error("HttpCommand.doHttpInvoke Failed to webClient head execute, traceId: {}, uri: {}, cause:{}", traceId, uri, Throwables.getStackTraceAsString(e)))
                            .timeout(Duration.ofMillis(timeout))
                            .flatMap(this::doNext);
        } else if (requestDTO.getHttpMethod().equals(HttpMethodEnum.PUT.getName())) {

            return webClient.put().uri(f -> uri)
                            .headers(httpHeaders -> {
                                httpHeaders.add(Constants.TRACE_ID, traceId);
                                httpHeaders.addAll(exchange.getRequest().getHeaders());
                            })
                            .contentType(buildMediaType())
                            .body(BodyInserters.fromDataBuffers(exchange.getRequest().getBody()))
                            .exchange()
                            .doOnError(e -> log.error("HttpCommand.doHttpInvoke Failed to webClient put execute, traceId: {}, uri: {}, cause:{}", traceId, uri, Throwables.getStackTraceAsString(e)))
                            .timeout(Duration.ofMillis(timeout))
                            .flatMap(this::doNext);

        } else if (requestDTO.getHttpMethod().equals(HttpMethodEnum.DELETE.getName())) {

            return webClient.delete().uri(f -> uri)
                            .headers(httpHeaders -> {
                                httpHeaders.add(Constants.TRACE_ID, traceId);
                                httpHeaders.addAll(exchange.getRequest().getHeaders());
                            })
                            .exchange()
                            .doOnError(e -> log.error("HttpCommand.doHttpInvoke Failed to webClient delete execute, traceId: {}, uri: {}, cause:{}", traceId, uri, Throwables.getStackTraceAsString(e)))
                            .timeout(Duration.ofMillis(timeout))
                            .flatMap(this::doNext);

        } else if (requestDTO.getHttpMethod().equals(HttpMethodEnum.PATCH.getName())) {
            return webClient.patch().uri(f -> uri)
                            .headers(httpHeaders -> {
                                httpHeaders.add(Constants.TRACE_ID, traceId);
                                httpHeaders.addAll(exchange.getRequest().getHeaders());
                            })
                            .contentType(buildMediaType())
                            .body(BodyInserters.fromDataBuffers(exchange.getRequest().getBody()))
                            .exchange()
                            .doOnError(e -> log.error("HttpCommand.doHttpInvoke Failed to webClient patch execute, traceId: {}, uri: {}, cause:{}", traceId, uri, Throwables.getStackTraceAsString(e)))
                            .timeout(Duration.ofMillis(timeout))
                            .flatMap(this::doNext);
        }

        log.warn("HttpCommand doHttpInvoke Waring no match doHttpInvoke end, traceId: {}, httpMethod: {}, uri: {}", traceId, requestDTO.getHttpMethod(), uri.getPath());

        return Mono.empty();
    }

    private URI buildRealURL(final DivideUpstream divideUpstream, final ServerWebExchange exchange) {
        String protocol = divideUpstream.getProtocol();
        if (StringUtils.isBlank(protocol)) {
            protocol = "http://";
        }

        StringBuilder builder = new StringBuilder(protocol);

        URI uri = exchange.getRequest().getURI();
        builder.append(divideUpstream.getUpstreamUrl().trim()).append(uri.getRawPath());

        if (!Strings.isNullOrEmpty(uri.getRawQuery())) {
            builder.append("?").append(uri.getRawQuery());
        }

        if (!Strings.isNullOrEmpty(uri.getRawFragment())) {
            builder.append("#").append(uri.getRawFragment());
        }

        Object diabloUserId = exchange.getAttributes().get(Constants.DIABLOUSERID);
        if (!Objects.isNull(diabloUserId)) {
            builder.append("&").append("diabloUserId="+diabloUserId);
        }

        URI newURI;
        try {
            newURI = new URI(builder.toString());
        } catch (URISyntaxException e) {
            log.error("HttpCommand.buildRealURL Failed URI Syntax error, newURI:{}, cause:{}", builder.toString(), Throwables.getStackTraceAsString(e));

            return null;
        }

        return newURI;
    }

    private String pathVariable(final String uri) {
        String path = uri;
        final String pathVariable = requestDTO.getPathVariable();
        if (StringUtils.isNoneBlank(pathVariable)) {
            path = path + "/" + pathVariable;
        }
        return path;
    }

    private MediaType buildMediaType() {
        return MediaType.valueOf(Optional.ofNullable(exchange
                .getRequest()
                .getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
                                         .orElse(MediaType.APPLICATION_JSON_UTF8_VALUE));
    }

    private Mono<Void> doNext(final ClientResponse res) {

        //access logø
        exchange.getAttributes().put(Constants.CLIENT_RESPONSE_UPSTREAM_REQUEST_TIME, this.requestStartTime);
        exchange.getAttributes().put(Constants.CLIENT_RESPONSE_UPSTREAM_RESPONSE_TIME, System.currentTimeMillis());
        exchange.getAttributes().put(Constants.CLIENT_RESPONSE_UPSTREAM_ADDR, this.divideUpstream.getUpstreamUrl());

        HttpHeaders httpHeaders = res.headers().asHttpHeaders();
        exchange.getResponse().getHeaders().putAll(httpHeaders);

        if (!httpHeaders.containsKey(HttpHeaders.TRANSFER_ENCODING)
                && httpHeaders.containsKey(HttpHeaders.CONTENT_LENGTH)) {
            exchange.getResponse().getHeaders().remove(HttpHeaders.TRANSFER_ENCODING);
        }

        if (!exchange.getResponse().isCommitted()) {
            int httpCode = res.statusCode().value();

            HttpStatus httpStatus = HttpStatus.resolve(httpCode);
            if (httpStatus != null) {
                exchange.getResponse().setStatusCode(httpStatus);
            } else if (exchange.getResponse() instanceof AbstractServerHttpResponse) {
                ((AbstractServerHttpResponse) exchange.getResponse()).setStatusCodeValue(httpCode);
            } else {
                throw new IllegalStateException("HttpCommand doNext Unable to set status code on response: " + httpCode + ", " + exchange.getResponse().getClass());
            }

            if (httpStatus.is3xxRedirection()) {
                exchange.getResponse().getHeaders().set(HttpHeaders.LOCATION, res.headers().asHttpHeaders().getFirst(HttpHeaders.LOCATION));
            }
        } else {
            log.warn("HttpCommand.doNext response is commited, traceId: {}, uri: {}", traceId, exchange.getRequest().getURI().getPath());
        }

        exchange.getAttributes().put(Constants.CLIENT_RESPONSE_ATTR, res);
        return chain.execute(exchange);
    }

}
