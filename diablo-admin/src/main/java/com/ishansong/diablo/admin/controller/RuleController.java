package com.ishansong.diablo.admin.controller;

import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;
import com.ishansong.diablo.admin.dto.RuleDTO;
import com.ishansong.diablo.admin.dto.RuleUpstreamDTO;
import com.ishansong.diablo.admin.page.PageParameter;
import com.ishansong.diablo.admin.page.RuleCommonPager;
import com.ishansong.diablo.admin.query.RuleQuery;
import com.ishansong.diablo.admin.service.RuleService;
import com.ishansong.diablo.admin.vo.RuleVO;
import com.ishansong.diablo.core.model.DiabloAdminResult;
import com.ishansong.diablo.core.utils.GsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/rule")
public class RuleController {


    private final Type ruleUpstreamType = new TypeToken<List<RuleUpstreamDTO>>() {
    }.getType();

    private final RuleService ruleService;

    @Autowired(required = false)
    public RuleController(final RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping("")
    public DiabloAdminResult queryRules(final String selectorId, final Integer currentPage, final Integer pageSize) {
        try {
            RuleCommonPager<RuleVO> commonPager = ruleService.listByPage(new RuleQuery(selectorId, new PageParameter(currentPage, pageSize)));
            return DiabloAdminResult.success("query rules success", commonPager);
        } catch (Exception e) {
            return DiabloAdminResult.error("query rules exception");
        }
    }

    @GetMapping("/{id}")
    public DiabloAdminResult detailRule(@PathVariable("id") final String id) {
        try {
            RuleVO ruleVO = ruleService.findById(id);
            return DiabloAdminResult.success("detail rule success", ruleVO);
        } catch (Exception e) {
            return DiabloAdminResult.error("detail rule exception");
        }
    }

    @PostMapping("")
    public DiabloAdminResult createRule(@RequestBody final RuleDTO ruleDTO) {
        try {
            if (!checkUpstream(ruleDTO)) {
                return DiabloAdminResult.error("请选择路由主机");
            }

            Integer createCount = ruleService.createOrUpdate(ruleDTO);
            return DiabloAdminResult.success("create rule success", createCount);
        } catch (Exception e) {
            return DiabloAdminResult.error("create rule exception");
        }
    }

    @PutMapping("/{id}")
    public DiabloAdminResult updateRule(@PathVariable("id") final String id, @RequestBody final RuleDTO ruleDTO) {
        try {
            Objects.requireNonNull(ruleDTO);
            if (!checkUpstream(ruleDTO)) {
                return DiabloAdminResult.error("请选择路由主机");
            }

            ruleDTO.setId(id);

            Integer updateCount = ruleService.createOrUpdate(ruleDTO);
            return DiabloAdminResult.success("update rule success", updateCount);
        } catch (Exception e) {
            return DiabloAdminResult.error("update rule exception");
        }
    }

    @DeleteMapping("/batch")
    public DiabloAdminResult deleteRules(@RequestBody final List<String> ids) {
        try {
            Integer deleteCount = ruleService.delete(ids);
            return DiabloAdminResult.success("delete rule success", deleteCount);
        } catch (Exception e) {
            return DiabloAdminResult.error("delete rule exception");
        }
    }

    private boolean checkUpstream(RuleDTO ruleDTO) {
        if (ruleDTO.getUpstreamHandle() == null) {
            return true;
        }

        if (Strings.isNullOrEmpty(ruleDTO.getServiceInfoId())) {
            List<RuleUpstreamDTO> upstreams = Optional.ofNullable(ruleDTO.getUpstreamHandle()).map(u -> (List<RuleUpstreamDTO>) GsonUtils.getInstance().fromJson(u, ruleUpstreamType)).orElse(null);

            if (CollectionUtils.isEmpty(upstreams)) {
                return false;
            }

            for (RuleUpstreamDTO upstream : upstreams) {
                if (Strings.isNullOrEmpty(upstream.getUpstreamHost()) || Strings.isNullOrEmpty(upstream.getUpstreamUrl())) {
                    return false;
                }
            }
        }

        return true;
    }

}
