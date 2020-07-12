package com.ishansong.diablo.admin.controller;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.ishansong.diablo.admin.dto.DubboResourceDTO;
import com.ishansong.diablo.admin.listener.CacheService;
import com.ishansong.diablo.admin.listener.DataChangedEvent;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.page.PageParameter;
import com.ishansong.diablo.admin.query.DubboResourceQuery;
import com.ishansong.diablo.admin.service.DubboResourceService;
import com.ishansong.diablo.admin.utils.AuditLogUtil;
import com.ishansong.diablo.admin.vo.DubboResourceDetailVO;
import com.ishansong.diablo.admin.vo.DubboResourceVO;
import com.ishansong.diablo.core.enums.ConfigGroupEnum;
import com.ishansong.diablo.core.enums.DataEventTypeEnum;
import com.ishansong.diablo.core.model.DiabloAdminResult;
import com.ishansong.diablo.core.model.dubbo.ApiConfig;
import com.ishansong.diablo.core.model.dubbo.DubboResourceData;
import com.ishansong.diablo.core.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/dubbo/resource")
public class DubboResourceController {

    private final DubboResourceService dubboResourceService;

    private final CacheService cacheService;

    private final ApplicationEventPublisher eventPublisher;

    @Autowired(required = false)
    public DubboResourceController(final DubboResourceService dubboResourceService, final CacheService cacheService, final ApplicationEventPublisher eventPublisher) {
        this.dubboResourceService = dubboResourceService;
        this.cacheService = cacheService;
        this.eventPublisher = eventPublisher;
    }

    @GetMapping("")
    public DiabloAdminResult list(@RequestParam(name = "serviceName", required = false) String serviceName,
                             @RequestParam(name = "namespace", required = false) String namespace,
                             @RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                             @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize) {

        if (Strings.isNullOrEmpty(serviceName)) {
            return DiabloAdminResult.error("请输入服务名称");
        }

        CommonPager<DubboResourceVO> dubboResources = dubboResourceService.listByPage(
                new DubboResourceQuery(serviceName, namespace, new PageParameter(currentPage, pageSize)));

        return DiabloAdminResult.success(dubboResources);
    }

    @GetMapping("/{id}")
    public DiabloAdminResult detail(@NotNull(message = "资源id为空") @PathVariable(required = false) String id) {

        DubboResourceDetailVO dubboResourceVO = dubboResourceService.findById(id);

        return DiabloAdminResult.success(dubboResourceVO);
    }

    @PostMapping("")
    public DiabloAdminResult save(@Validated @RequestBody DubboResourceDTO dubboResourceDto, HttpServletRequest request) {

        long durationStart = System.nanoTime();
        assignResourceList(dubboResourceDto);

        Integer saveCount = dubboResourceService.save(dubboResourceDto);

        String authorized = request.getHeader("Authorized-Info");
        AuditLogUtil.reportedTransaction(durationStart, "新增映射", dubboResourceDto.getKey(), authorized, GsonUtils.getInstance().toJson(dubboResourceDto));

        return DiabloAdminResult.success("dubbo resource save success", saveCount);
    }

    @PutMapping("")
    public DiabloAdminResult update(@Validated @RequestBody DubboResourceDTO dubboResourceDto, HttpServletRequest request) {

        long durationStart = System.nanoTime();

        assignResourceList(dubboResourceDto);

        int updateCount = dubboResourceService.update(dubboResourceDto);

        String authorized = request.getHeader("Authorized-Info");
        AuditLogUtil.reportedTransaction(durationStart, "修改映射", dubboResourceDto.getKey(), authorized, GsonUtils.getInstance().toJson(dubboResourceDto));

        return DiabloAdminResult.success("dubbo resource update success", updateCount);
    }


    @DeleteMapping("")
    public DiabloAdminResult delete(@RequestBody final List<String> ids, HttpServletRequest request) {

        long durationStart = System.nanoTime();

        int deleteCount = dubboResourceService.delete(ids);

        String authorized = request.getHeader("Authorized-Info");
        AuditLogUtil.reportedTransaction(durationStart, "删除映射", "", authorized, GsonUtils.getInstance().toJson(ids));

        return DiabloAdminResult.success("dubbo resource delete success", deleteCount);
    }

    @GetMapping("/sync")
    public DiabloAdminResult sync(HttpServletRequest request) {

        long durationStart = System.nanoTime();

        List<DubboResourceData> dubboResources = cacheService.loadDubboResourceCache();

        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.DUBBO_MAPPING, DataEventTypeEnum.REFRESH, dubboResources));

        String authorized = request.getHeader("Authorized-Info");
        AuditLogUtil.reportedTransaction(durationStart, "同步映射", "", authorized, "");

        return DiabloAdminResult.success("dubbo resource sync success", dubboResources);
    }

    private void assignResourceList(DubboResourceDTO dubboResourceDto) {
        if (!Strings.isNullOrEmpty(dubboResourceDto.getAllowDomainStr())) {

            List<String> allowDomains = Splitter.on(",").splitToList(dubboResourceDto.getAllowDomainStr());
            ApiConfig apiConfig = dubboResourceDto.getApiConfig();
            if (apiConfig == null) {
                apiConfig = new ApiConfig();
            }

            apiConfig.setAllowDomain(allowDomains);

        }

        if (!Strings.isNullOrEmpty(dubboResourceDto.getRedisKeyNameStr())) {

            List<String> redisKeyNames = Splitter.on(",").splitToList(dubboResourceDto.getRedisKeyNameStr());
            ApiConfig apiConfig = dubboResourceDto.getApiConfig();
            if (apiConfig == null) {
                apiConfig = new ApiConfig();
            }

            apiConfig.setRedisKeyName(redisKeyNames);
        }
    }
}
