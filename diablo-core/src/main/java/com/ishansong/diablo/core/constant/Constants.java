package com.ishansong.diablo.core.constant;

public interface Constants {

    String REQUESTDTO = "requestDTO";

    String TRACE_ID = "TRACE_ID";

    String CLIENT_RESPONSE_ATTR = "webHandlerClientResponse";

    String DUBBO_RPC_RESULT = "dubbo_rpc_result";

    String DUBBO_RPC_EXCEPTION= "dubbo_rpc_exception";

    String DUBBO_TOKEN_RPC_TIMEOUT= "dubbo_token_rpc_timeout";

    String CLIENT_RESPONSE_UPSTREAM_ADDR = "webHandlerClientResponseUpstreamAddr";

    String CLIENT_RESPONSE_TRACE_ID = "webHandlerClientResponseTraceId";

    String CLIENT_RESPONSE_UPSTREAM_REQUEST_TIME = "webHandlerClientResponseUpstreamRequestTime";

    String CLIENT_RESPONSE_UPSTREAM_RESPONSE_TIME = "webHandlerClientResponseUpstreamResponseTime";

    String ACCESS_LOG_DEFAULT_PARAMS_STRING = "-";

    String DUBBO_PARAMS = "dubbo_params";

    String DECODE = "UTF-8";

    String MODULE = "module";

    String METHOD = "method";

    String APP_KEY = "appKey";

    String EXT_INFO = "extInfo";

    String PATH_VARIABLE = "pathVariable";

    String RPC_TYPE = "rpcType";

    String RESOURCE_KEY = "resourceKey";

    String TOKEN = "token";

    String DIABLOUSERID = "diabloUserId";

    String SIGN = "sign";

    String HEADER_CLIENT_ID = "Client-Id";

    String TIMESTAMP = "timestamp";

    String REJECT_MSG = " You are forbidden to visit";

    String HTTP_ERROR_RESULT = "this is bad request or fuse ing please try again later";

    String DUBBO_ERROR_RESULT = "Rpc call failed please check the parameters";

    String TIMEOUT_RESULT = "this request is time out Please try again later";

    String UPSTREAM_NOT_FIND = "No list of available services was found";

    int TIME_OUT = 3000;

    String COLONS = ":";

    String TRANSACTION_TYPE_URL = "URL";

    String TRANSACTION_TYPE_KEEP_ALIVE = "KEEP_ALIVE";

    String TRANSACTION_TYPE_RESPONSE_STATUS = "Response_Status";
    String TRANSACTION_TYPE_RESPONSE_NULL = "Response_Null";
    String TRANSACTION_TYPE_DUBBO_RESPONSE_EMPTY = "Dubbo_Response_Empty";

    String DUBBO_TRANSACTION_TYPE_URL = "Dubbo_";
    String DUBBO_CALL_SERVICE_NAME = "dubboServiceName";

    String GATEWAY_CONTEXT_PREFIX = "sentinel_gateway_context$$";
    String GATEWAY_CONTEXT_ROUTE_PREFIX = "sentinel_gateway_context$$route$$";
    String GATEWAY_CONTEXT_UPSTREAM_HOST = GATEWAY_CONTEXT_PREFIX + "upstreamHost";
    String GATEWAY_CONTEXT_RULE_ID = GATEWAY_CONTEXT_PREFIX + "ruleId";
    String GATEWAY_CONTEXT_API_NAME = GATEWAY_CONTEXT_PREFIX + "apiName";
    String GATEWAY_ALREADY_ROUTED_ATTR = "gateway_Already_Routed";

    int RESOURCE_MODE_ROUTE_ID = 0;
    int RESOURCE_MODE_CUSTOM_API_NAME = 1;

}

