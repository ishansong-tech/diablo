package com.ishansong.diablo.admin.controller;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ishansong.diablo.admin.dto.SelectorDTO;
import com.ishansong.diablo.admin.page.CommonPager;
import com.ishansong.diablo.admin.page.PageParameter;
import com.ishansong.diablo.admin.pre.service.PrepareService;
import com.ishansong.diablo.admin.query.SelectorQuery;
import com.ishansong.diablo.admin.service.SelectorService;
import com.ishansong.diablo.admin.vo.SelectorBackupVO;
import com.ishansong.diablo.admin.vo.SelectorBackupVersionVO;
import com.ishansong.diablo.admin.vo.SelectorPublishVO;
import com.ishansong.diablo.admin.vo.SelectorVO;
import com.ishansong.diablo.core.constant.AdminConstants;
import com.ishansong.diablo.core.model.DiabloAdminResult;
import com.ishansong.diablo.core.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/selector")
public class SelectorController {

    private final SelectorService selectorService;

    private final PrepareService prepareService;

    @Autowired(required = false)
    public SelectorController(final SelectorService selectorService, PrepareService prepareService) {
        this.selectorService = selectorService;
        this.prepareService = prepareService;
    }

    @GetMapping("")
    public DiabloAdminResult querySelectors(final String pluginId, final Integer currentPage, final Integer pageSize) {
        try {
            CommonPager<SelectorVO> commonPager = selectorService.listByPage(new SelectorQuery(pluginId, new PageParameter(currentPage, pageSize)));
            return DiabloAdminResult.success("query selectors success", commonPager);
        } catch (Exception e) {
            return DiabloAdminResult.error("query selectors exception");
        }
    }

    @GetMapping("/{id}")
    public DiabloAdminResult detailSelector(@PathVariable("id") final String id) {
        try {
            SelectorVO selectorVO = selectorService.findById(id);
            return DiabloAdminResult.success("detail selector success", selectorVO);
        } catch (Exception e) {
            e.printStackTrace();
            return DiabloAdminResult.error("detail selector exception");
        }
    }

    @GetMapping("/rollbackVersion/{id}")
    public DiabloAdminResult rollbackVersion(@PathVariable("id") String id, @RequestParam(value = "currentPage", defaultValue = "0") Integer currentPage,
                                        @RequestParam(value = "pageSize", defaultValue = "15") Integer pageSize) {

        List<SelectorBackupVersionVO> backupVO = prepareService.queryBackupVersionById(id, currentPage, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("rollbacks", backupVO);


        if (!CollectionUtils.isEmpty(backupVO)) {
            result.put("backupVersion", backupVO.get(0));
        } else {
            result.put("backupVersion", new SelectorBackupVersionVO());
        }

        return DiabloAdminResult.success("detail selector success", result);
    }

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @GetMapping("/rollbackVersion/{id}/{timestamp}")
    public String rollbackVersion(@PathVariable("id") String id, @PathVariable("timestamp") Long timestamp) {

        SelectorBackupVO backupVO = prepareService.queryBackupByIdAndTimestamp(id, timestamp);

        return gson.toJson(DiabloAdminResult.success("detail selector success", backupVO));
    }

    @PostMapping("")
    public DiabloAdminResult createSelector(@RequestBody final SelectorDTO selectorDTO) {
        try {
            Integer createCount = selectorService.createOrUpdate(selectorDTO);
            return DiabloAdminResult.success("create selector success", createCount);
        } catch (Exception e) {
            return DiabloAdminResult.error("create selector exception");
        }
    }

    @PutMapping("/{id}")
    public DiabloAdminResult updateSelector(@PathVariable("id") final String id, @RequestBody final SelectorDTO selectorDTO) {
        try {
            Objects.requireNonNull(selectorDTO);
            selectorDTO.setId(id);
            Integer updateCount = selectorService.createOrUpdate(selectorDTO);
            return DiabloAdminResult.success("update selector success", updateCount);
        } catch (Exception e) {
            return DiabloAdminResult.error("update selector exception");
        }
    }

    @DeleteMapping("/batch")
    public DiabloAdminResult deleteSelector(@RequestBody final List<String> ids) {
        try {
            Integer deleteCount = selectorService.delete(ids);
            return DiabloAdminResult.success("delete selectors success", deleteCount);
        } catch (Exception e) {
            return DiabloAdminResult.error("delete selectors exception");
        }
    }

    @PostMapping("/publish")
    public DiabloAdminResult publishSelector(@RequestBody SelectorPublishVO selectorPublishVO, HttpServletRequest request) {

        Map<String, String> publish = null;
        String authorized = request.getHeader("Authorized-Info");

        String remark = selectorPublishVO.getRemark();
        if (Strings.isNullOrEmpty(remark)) {
            return DiabloAdminResult.error("请填写发布描述信息");
        }

        remark += " by " + authorized;

        long nanoTime = System.nanoTime();
        try {
            publish = prepareService.publish(Collections.singletonList(selectorPublishVO.getId()), remark);
            String message = publish.get(AdminConstants.OPERATE_RESULT_CODE);
            if (!Strings.isNullOrEmpty(message)) {
                return DiabloAdminResult.error(message);
            }

            return DiabloAdminResult.success("publish success", GsonUtils.getInstance().toJson(publish));
        } catch (Exception e) {
            return DiabloAdminResult.error("发布失败", Throwables.getStackTraceAsString(e));
        } finally {
            HttpSession session = request.getSession(false);
            // log level
            String loginName = "";
            if (session != null) {
                loginName = Optional.ofNullable(session.getAttribute(AdminConstants.SESSION_LOGIN_NAME)).map(Object::toString).orElse("-");
            }

            log.warn("SelectorController publishSelector ids={}, loginName={}", selectorPublishVO.getId(), authorized);

            reportedTransaction(publish, nanoTime, authorized, "发布");
        }
    }

    @PostMapping("/rollback")
    public DiabloAdminResult rollbackSelector(@RequestBody final SelectorBackupVersionVO selectorBackupVersionVO, HttpServletRequest request) {

        Map<String, String> rollback = null;
        long nanoTime = System.nanoTime();
        try {
            rollback = prepareService.rollback(selectorBackupVersionVO.getId(), selectorBackupVersionVO.getTimestamp());

            String message = rollback.get(AdminConstants.OPERATE_RESULT_CODE);
            if (!Strings.isNullOrEmpty(message)) {
                return DiabloAdminResult.error(message);
            }

            return DiabloAdminResult.success("rollback success", GsonUtils.getInstance().toJson(rollback));
        } catch (Exception e) {
            return DiabloAdminResult.error("回滚失败", Throwables.getStackTraceAsString(e));
        } finally {
            HttpSession session = request.getSession(false);

            String loginName = "";
            if (session != null) {
                loginName = Optional.ofNullable(session.getAttribute(AdminConstants.SESSION_LOGIN_NAME)).map(Object::toString).orElse("-");
            }

            String authorized = request.getHeader("Authorized-Info");

            log.warn("SelectorController rollbackSelector id={}, timestamp={}, loginName={}", selectorBackupVersionVO.getId(), selectorBackupVersionVO.getTimestamp(), authorized);

            reportedTransaction(rollback, nanoTime, authorized, "回滚");
        }
    }

    private void reportedTransaction(Map<String, String> map, long durationStart, String authorized, String type) {
        String selectorName = null;
        if (map != null && map.get(AdminConstants.SELECTOR_NAME) != null) {
            selectorName = map.get(AdminConstants.SELECTOR_NAME);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
        String name = new StringJoiner("_").add(selectorName).add(type).add(formatter.format(LocalDateTime.now())).add(authorized).toString();

        DefaultTransaction transaction = (DefaultTransaction) Cat.newTransaction("Audit_Log", name);
        transaction.setDurationStart(durationStart);

        transaction.setStatus(Message.SUCCESS);
        transaction.complete();
    }
}
