package com.ishansong.diablo.admin.controller;

import com.google.common.collect.Maps;
import com.ishansong.diablo.admin.listener.http.HttpLongPollingDataChangedListener;
import com.ishansong.diablo.core.enums.ConfigGroupEnum;
import com.ishansong.diablo.core.model.ConfigData;
import com.ishansong.diablo.core.model.DiabloAdminResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/configs")
@Slf4j
public class ConfigController {

    @Resource
    private HttpLongPollingDataChangedListener longPollingListener;

    @GetMapping("/fetch")
    public DiabloAdminResult fetchConfigs(@RequestParam(value = "groupKeys") String[] groupKeyValues) {
        try {
            Map<String, ConfigData> result = Maps.newConcurrentMap();
            for (String groupKey : groupKeyValues) {
                ConfigData data = longPollingListener.fetchConfig(ConfigGroupEnum.valueOf(groupKey));
                result.put(groupKey, data);
            }
            return DiabloAdminResult.success("success", result);
        } catch (Exception e) {
            log.error("fetch all configs error.", e);
            return DiabloAdminResult.error("fetch all configs error: " + e.getMessage());
        }
    }

    @PostMapping(value = "/listener")
    public void listener(HttpServletRequest request, HttpServletResponse response) {
        longPollingListener.doLongPolling(request, response);
    }

}
