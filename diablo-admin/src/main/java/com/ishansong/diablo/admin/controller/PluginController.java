package com.ishansong.diablo.admin.controller;

import com.ishansong.diablo.admin.dto.BatchCommonDTO;
import com.ishansong.diablo.admin.dto.PluginDTO;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.page.PageParameter;
import com.ishansong.diablo.admin.query.PluginQuery;
import com.ishansong.diablo.admin.service.PluginService;
import com.ishansong.diablo.admin.service.SyncDataService;
import com.ishansong.diablo.admin.vo.PluginVO;
import com.ishansong.diablo.core.enums.DataEventTypeEnum;
import com.ishansong.diablo.core.model.DiabloAdminResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/plugin")
public class PluginController {

    private final PluginService pluginService;

    private final SyncDataService syncDataService;

    @Autowired(required = false)
    public PluginController(final PluginService pluginService,
                            final SyncDataService syncDataService) {
        this.pluginService = pluginService;
        this.syncDataService = syncDataService;
    }

    @GetMapping("")
    public DiabloAdminResult queryPlugins(final String name, final Integer currentPage, final Integer pageSize) {
        try {
            CommonPager<PluginVO> commonPager = pluginService.listByPage(new PluginQuery(name, new PageParameter(currentPage, pageSize)));
            return DiabloAdminResult.success("query plugins success", commonPager);
        } catch (Exception e) {
            return DiabloAdminResult.error("query plugins exception");
        }
    }

    @GetMapping("/{id}")
    public DiabloAdminResult detailPlugin(@PathVariable("id") final String id) {
        try {
            PluginVO pluginVO = pluginService.findById(id);
            return DiabloAdminResult.success("detail plugin success", pluginVO);
        } catch (Exception e) {
            return DiabloAdminResult.error("detail plugin exception");
        }
    }

    @PostMapping("")
    public DiabloAdminResult createPlugin(@RequestBody final PluginDTO pluginDTO) {
        try {
            String result = pluginService.createOrUpdate(pluginDTO);
            if (StringUtils.isNoneBlank()) {
                return DiabloAdminResult.error(result);
            }
            return DiabloAdminResult.success("create plugin success");
        } catch (Exception e) {
            return DiabloAdminResult.error("create plugin exception");
        }
    }

    @PutMapping("/{id}")
    public DiabloAdminResult updatePlugin(@PathVariable("id") final String id, @RequestBody final PluginDTO pluginDTO) {
        try {
            Objects.requireNonNull(pluginDTO);
            pluginDTO.setId(id);
            final String result = pluginService.createOrUpdate(pluginDTO);
            if (StringUtils.isNoneBlank(result)) {
                return DiabloAdminResult.error(result);
            }
            return DiabloAdminResult.success("update plugin success");
        } catch (Exception e) {
            return DiabloAdminResult.error("update plugin exception");
        }
    }

    @DeleteMapping("/batch")
    public DiabloAdminResult deletePlugins(@RequestBody final List<String> ids) {
        try {
            final String result = pluginService.delete(ids);
            if (StringUtils.isNoneBlank(result)) {
                return DiabloAdminResult.error(result);
            }
            return DiabloAdminResult.success("delete plugins success");
        } catch (Exception e) {
            return DiabloAdminResult.error("delete plugins exception");
        }
    }

    @PostMapping("/enabled")
    public DiabloAdminResult enabled(@RequestBody final BatchCommonDTO batchCommonDTO) {
        try {
            final String result = pluginService.enabled(batchCommonDTO.getIds(), batchCommonDTO.getEnabled());
            if (StringUtils.isNoneBlank(result)) {
                return DiabloAdminResult.error(result);
            }
            return DiabloAdminResult.success("enable plugins success");
        } catch (Exception e) {
            return DiabloAdminResult.error("enable plugins exception");
        }
    }

    @PostMapping("/syncPluginAll")
    public DiabloAdminResult syncPluginAll() {
        try {
            boolean success = syncDataService.syncAll(DataEventTypeEnum.REFRESH);
            if (success) {
                return DiabloAdminResult.success("sync plugins success");
            } else {
                return DiabloAdminResult.success("sync plugins fail");
            }
        } catch (Exception e) {
            return DiabloAdminResult.error("sync plugins exception");
        }
    }

    @PutMapping("/syncPluginData/{id}")
    public DiabloAdminResult syncPluginData(@PathVariable("id") final String id) {
        try {
            boolean success = syncDataService.syncPluginData(id);
            if (success) {
                return DiabloAdminResult.success("sync plugins success");
            } else {
                return DiabloAdminResult.success("sync plugins fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DiabloAdminResult.error("sync plugins exception{}");
        }
    }
}
