package com.ishansong.diablo.extension.sentinel.adapter.callback;

import com.alibaba.csp.sentinel.util.AssertUtil;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Function;

public final class GatewayCallbackManager {

    private static final Function<ServerWebExchange, String> DEFAULT_ORIGIN_PARSER = (w) -> "";

    /**
     * BlockRequestHandler: (serverExchange, exception) -> response
     */
    private static volatile BlockRequestHandler blockHandler = new DefaultBlockRequestHandler();
    /**
     * RequestOriginParser: (serverExchange) -> origin
     */
    private static volatile Function<ServerWebExchange, String> requestOriginParser = DEFAULT_ORIGIN_PARSER;

    public static /*@NonNull*/ BlockRequestHandler getBlockHandler() {
        return blockHandler;
    }

    public static void resetBlockHandler() {
        GatewayCallbackManager.blockHandler = new DefaultBlockRequestHandler();
    }

    public static void setBlockHandler(BlockRequestHandler blockHandler) {
        AssertUtil.notNull(blockHandler, "blockHandler cannot be null");
        GatewayCallbackManager.blockHandler = blockHandler;
    }

    public static /*@NonNull*/ Function<ServerWebExchange, String> getRequestOriginParser() {
        return requestOriginParser;
    }

    public static void resetRequestOriginParser() {
        GatewayCallbackManager.requestOriginParser = DEFAULT_ORIGIN_PARSER;
    }

    public static void setRequestOriginParser(Function<ServerWebExchange, String> requestOriginParser) {
        AssertUtil.notNull(requestOriginParser, "requestOriginParser cannot be null");
        GatewayCallbackManager.requestOriginParser = requestOriginParser;
    }

    private GatewayCallbackManager() {}
}
