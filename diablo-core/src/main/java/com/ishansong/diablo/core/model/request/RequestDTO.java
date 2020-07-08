package com.ishansong.diablo.core.model.request;

import com.google.common.base.Strings;
import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.enums.HttpMethodEnum;
import com.ishansong.diablo.core.enums.RpcTypeEnum;
import com.ishansong.diablo.core.utils.UrlUtils;
import lombok.Data;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.util.Optional;

@Data
public class RequestDTO implements Serializable {

    private String module;

    private String method;

    private String rpcType;

    private String httpMethod;

    private String dubboParams;

    private String sign;

    private String token;

    private String clientId;

    private String timestamp;

    private String appKey;

    private String content;

    private String extInfo;

    private String pathVariable;

    private long startDateTime;

    private long durationStart;

    public static RequestDTO transform(final ServerHttpRequest request) {
        final String module = Optional.ofNullable(request.getHeaders().getFirst(Constants.MODULE)).orElse("gateway");
        final String method = Optional.ofNullable(request.getHeaders().getFirst(Constants.METHOD)).orElse(request.getPath().toString());
        final String appKey = request.getHeaders().getFirst(Constants.APP_KEY);

        String clientId = request.getHeaders().getFirst(Constants.HEADER_CLIENT_ID);
        final String httpMethod = request.getMethod().name().toLowerCase();
        String queryString = request.getURI().getQuery();
        final String rpcType = Optional.ofNullable((Strings.isNullOrEmpty(request.getHeaders().getFirst(Constants.RPC_TYPE)) ? UrlUtils.getQueryString(queryString,Constants.RPC_TYPE) : request.getHeaders().getFirst(Constants.RPC_TYPE))).orElse("http");
        final String token =  UrlUtils.getQueryString(queryString,Constants.TOKEN);
        final String sign = request.getHeaders().getFirst(Constants.SIGN);
        final String timestamp = request.getHeaders().getFirst(Constants.TIMESTAMP);
        final String extInfo = request.getHeaders().getFirst(Constants.EXT_INFO);
        final String pathVariable = request.getHeaders().getFirst(Constants.PATH_VARIABLE);

        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setModule(module);
        requestDTO.setMethod(method);
        requestDTO.setAppKey(appKey);
        requestDTO.setHttpMethod(httpMethod);
        requestDTO.setRpcType(rpcType);
        requestDTO.setSign(sign);
        requestDTO.setClientId(clientId);
        requestDTO.setToken(token);
        requestDTO.setTimestamp(timestamp);
        requestDTO.setExtInfo(extInfo);
        requestDTO.setPathVariable(pathVariable);
        requestDTO.setDurationStart(System.nanoTime());
        requestDTO.setStartDateTime(System.currentTimeMillis());

        return requestDTO;
    }

    public static RequestDTO transformMap(MultiValueMap<String, String> queryParams) {
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setModule(queryParams.getFirst(Constants.MODULE));
        requestDTO.setMethod(queryParams.getFirst(Constants.METHOD));
        requestDTO.setRpcType(queryParams.getFirst(Constants.RPC_TYPE));
        return requestDTO;
    }

}
