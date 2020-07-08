package com.ishansong.diablo.plugin.condition.strategy;

import com.ishansong.diablo.core.constant.Constants;
import com.ishansong.diablo.core.enums.ParamTypeEnum;
import com.ishansong.diablo.core.model.condition.ConditionData;
import com.ishansong.diablo.core.model.request.RequestDTO;
import com.ishansong.diablo.core.utils.ReflectUtils;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

abstract class AbstractMatchStrategy {

    String buildRealData(final ConditionData condition, final ServerWebExchange exchange) {
        String realData = "";
        if (ParamTypeEnum.URI.getName().equals(condition.getParamType())) {
            realData = exchange.getRequest().getPath().value();
        } else if (ParamTypeEnum.COOKIE.getName().equals(condition.getParamType())) {
            realData = Optional.ofNullable(exchange.getRequest().getCookies().getFirst(condition.getParamName())).map(HttpCookie::getValue).orElse("");
        } else if (Objects.equals(ParamTypeEnum.HOST.getName(), condition.getParamType())) {
//            Optional<InetSocketAddress> host = Optional.ofNullable(exchange.getRequest().getHeaders().getHost());
//            Integer port = host.map(InetSocketAddress::getPort).orElse(0);
//
//            realData = host.map(InetSocketAddress::getHostName).orElse("");
//            if (port > 0) {
//                realData = realData + ":" + port;
//            }

            realData = exchange.getRequest().getHeaders().getFirst("Host");
        } else if (condition.getParamType().equals(ParamTypeEnum.QUERY.getName())) {
            final MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
            realData = queryParams.getFirst(condition.getParamName());
        } else if (Objects.equals(ParamTypeEnum.HEADER.getName(), condition.getParamType())) {
            final HttpHeaders headers = exchange.getRequest().getHeaders();
            final List<String> list = headers.get(condition.getParamName());
            if (CollectionUtils.isEmpty(list)) {
                return realData;
            }
            realData = Objects.requireNonNull(headers.get(condition.getParamName())).stream().findFirst().orElse("");
        } else if (Objects.equals(ParamTypeEnum.IP.getName(), condition.getParamType())) {
            realData = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
        } else if (condition.getParamType().equals(ParamTypeEnum.POST.getName())) {
            final RequestDTO requestDTO = exchange.getAttribute(Constants.REQUESTDTO);
            realData = (String) ReflectUtils.getFieldValue(requestDTO, condition.getParamName());
        }
        return realData;
    }

}
