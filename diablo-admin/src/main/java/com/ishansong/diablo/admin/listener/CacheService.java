package com.ishansong.diablo.admin.listener;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.SerialPolicy;
import com.google.common.base.Throwables;
import com.ishansong.diablo.admin.service.DubboResourceService;
import com.ishansong.diablo.admin.service.PluginService;
import com.ishansong.diablo.admin.service.RuleService;
import com.ishansong.diablo.admin.service.SelectorService;
import com.ishansong.diablo.core.enums.ConfigGroupEnum;
import com.ishansong.diablo.core.model.dubbo.DubboResourceData;
import com.ishansong.diablo.core.utils.GsonUtils;
import com.ishansong.diablo.core.utils.Md5Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    @CreateCache(expire = 60 * 60 * 24, name = "diablo.ConfigDataCache", cacheType = CacheType.LOCAL, serialPolicy = SerialPolicy.KRYO)
    private Cache<String, ConfigDataCache> configDataCache;

    @CreateCache(expire = 24 * 60 * 60, name = "diablo.DubboResource:", cacheType = CacheType.LOCAL, serialPolicy = SerialPolicy.KRYO)
    private Cache<String, List<DubboResourceData>> cacheDubboResourceData;

    @Resource
    private PluginService pluginService;

    @Resource
    private RuleService ruleService;

    @Resource
    private SelectorService selectorService;

    @Resource
    private DubboResourceService dubboResourceService;

    public Cache<String, ConfigDataCache> getConfigDataCacheService() {
        return this.configDataCache;
    }

    public void updateSelectorCache() {
        String data = GsonUtils.getInstance().toJson(selectorService.listAll());
        String group = ConfigGroupEnum.SELECTOR.name();

        updateCache(group, data);
    }

    public List<DubboResourceData> loadDubboResourceCache() {

        this.updateDubboResourceCache();

        return new ArrayList<>();
    }

    public void updateDubboResourceCache() {

        List<DubboResourceData> dubboResourceData = dubboResourceService.listAll();
        String data = GsonUtils.getInstance().toJson(dubboResourceData);
        String group = ConfigGroupEnum.DUBBO_MAPPING.name();

        updateCache(group, data);

        cacheDubboResourceData.put(group, dubboResourceData);
    }

    public void updateRuleCache() {
        String data = GsonUtils.getInstance().toJson(ruleService.listAll());
        String group = ConfigGroupEnum.RULE.name();

        updateCache(group, data);
    }

    public void updatePluginCache() {
        String group = ConfigGroupEnum.PLUGIN.name();

        String data = GsonUtils.getInstance().toJson(pluginService.listAll());

        updateCache(group, data);
    }

    private void updateCache(String group, String data) {
        try {
            if(true){
                return;

            }

            ConfigDataCache configDataCache = this.getConfigDataCacheService().get(group);

            String newMd5 = Md5Utils.md5(data);
            if (configDataCache == null) {
                logger.info("CacheService updateCache init, group:{}, json:{}", group, data);

                this.getConfigDataCacheService().put(group, new ConfigDataCache(group, newMd5, System.currentTimeMillis()));

                return;
            }

            String originMd5 = configDataCache.getMd5();

            if (!Objects.equals(originMd5, newMd5)) {
                logger.info("CacheService updateCache change, group:{}, json:{}", group, data);

                this.getConfigDataCacheService().put(group, new ConfigDataCache(group, newMd5, System.currentTimeMillis()));
            }

        } catch (Exception e) {
            logger.error("CacheService updateCache error, group:{}, cause:{}", group, Throwables.getStackTraceAsString(e));

            throw e;
        }
    }
}
