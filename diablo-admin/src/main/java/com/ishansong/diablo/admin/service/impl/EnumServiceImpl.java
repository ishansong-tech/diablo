package com.ishansong.diablo.admin.service.impl;

import com.google.common.collect.Maps;
import com.ishansong.diablo.admin.service.EnumService;
import com.ishansong.diablo.admin.vo.EnumVO;
import com.ishansong.diablo.core.enums.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("enumService")
public class EnumServiceImpl implements EnumService {

    @Override
    public Map<String, List<EnumVO>> list() {
        List<EnumVO> httpMethodEnums = Arrays.stream(HttpMethodEnum.values())
                .map(httpMethodEnum -> new EnumVO(null, httpMethodEnum.getName(), httpMethodEnum.getSupport()))
                .collect(Collectors.toList());

        List<EnumVO> loadBalanceEnums = Arrays.stream(LoadBalanceEnum.values())
                .map(loadBalanceEnum -> new EnumVO(loadBalanceEnum.getCode(), loadBalanceEnum.getName(), true))
                .collect(Collectors.toList());

        List<EnumVO> matchModeEnums = Arrays.stream(MatchModeEnum.values())
                .map(matchModeEnum -> new EnumVO(matchModeEnum.getCode(), matchModeEnum.getName(), true))
                .collect(Collectors.toList());

        List<EnumVO> operatorEnums =
                OperatorEnum.acquireSupport().stream().map(operatorEnum ->
                        new EnumVO(null, operatorEnum.getAlias(), operatorEnum.getSupport()))
                        .collect(Collectors.toList());

        List<EnumVO> paramTypeEnums = ParamTypeEnum.acquireSupport().stream()
                .map(paramTypeEnum -> new EnumVO(null, paramTypeEnum.getName(), paramTypeEnum.getSupport())).collect(Collectors.toList());

        List<EnumVO> pluginEnums = Arrays.stream(PluginEnum.values())
                .map(pluginEnum -> new EnumVO(pluginEnum.getCode(), pluginEnum.getName(), true))
                .collect(Collectors.toList());

        List<EnumVO> pluginTypeEnums = Arrays.stream(PluginTypeEnum.values())
                .map(pluginTypeEnum -> new EnumVO(null, pluginTypeEnum.getName(), true))
                .collect(Collectors.toList());

        List<EnumVO> rpcTypeEnums = RpcTypeEnum.acquireSupports().stream()
                .map(rpcTypeEnum -> new EnumVO(null, rpcTypeEnum.getName(), rpcTypeEnum.getSupport()))
                .collect(Collectors.toList());

        List<EnumVO> wafEnums = Arrays.stream(WafEnum.values())
                                     .map(w -> new EnumVO(w.getCode(), w.getName(), true))
                                     .collect(Collectors.toList());

        List<EnumVO> selectorTypeEnums = Arrays.stream(SelectorTypeEnum.values())
                .map(selectorTypeEnum -> new EnumVO(selectorTypeEnum.getCode(), selectorTypeEnum.getName(), true)).collect(Collectors.toList());

        Map<String, List<EnumVO>> enums = Maps.newHashMap();
        enums.put("httpMethodEnums", httpMethodEnums);
        enums.put("loadBalanceEnums", loadBalanceEnums);
        enums.put("matchModeEnums", matchModeEnums);
        enums.put("operatorEnums", operatorEnums);
        enums.put("paramTypeEnums", paramTypeEnums);
        enums.put("pluginEnums", pluginEnums);
        enums.put("pluginTypeEnums", pluginTypeEnums);
        enums.put("rpcTypeEnums", rpcTypeEnums);
        enums.put("wafEnums", wafEnums);
        enums.put("selectorTypeEnums", selectorTypeEnums);
        return enums;
    }
}
