package com.ishansong.diablo.admin.listener;

import com.ishansong.diablo.core.model.dubbo.DubboResourceData;
import com.ishansong.diablo.core.model.plugin.PluginData;
import com.ishansong.diablo.core.model.rule.RuleData;
import com.ishansong.diablo.core.model.selector.SelectorData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class DataChangedEventDispatcher implements ApplicationListener<DataChangedEvent>, InitializingBean {

    private ApplicationContext applicationContext;

    private List<DataChangedListener> listeners;

    public DataChangedEventDispatcher(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onApplicationEvent(final DataChangedEvent event) {

        Long durationStart = event.getDurationStart() != null ? event.getDurationStart() : System.nanoTime();
        for (DataChangedListener listener : listeners) {
            switch (event.getGroupKey()) {
                case PLUGIN:
                    listener.onPluginChanged((List<PluginData>) event.getSource(), event.getEventType(), durationStart);
                    break;
                case RULE:
                    listener.onRuleChanged((List<RuleData>) event.getSource(), event.getEventType(), durationStart);
                    break;
                case SELECTOR:
                    listener.onSelectorChanged((List<SelectorData>) event.getSource(), event.getEventType(), durationStart);
                    break;
                case DUBBO_MAPPING:
                    listener.onDubboResourceChanged((List<DubboResourceData>) event.getSource(), event.getEventType(), durationStart);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + event.getGroupKey());
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        Collection<DataChangedListener> listenerBeans = applicationContext.getBeansOfType(DataChangedListener.class).values();
        this.listeners = Collections.unmodifiableList(new ArrayList<>(listenerBeans));
    }

}
