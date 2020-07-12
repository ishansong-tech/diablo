package com.ishansong.diablo.admin.listener;

import com.ishansong.diablo.core.enums.ConfigGroupEnum;
import com.ishansong.diablo.core.enums.DataEventTypeEnum;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class DataChangedEvent extends ApplicationEvent {

    private ConfigGroupEnum groupKey;

    private DataEventTypeEnum eventType;

    private Long durationStart;

    public DataChangedEvent(ConfigGroupEnum groupKey, DataEventTypeEnum type, Long durationStart, List<?> source) {
        super(source);
        this.eventType = type;
        this.groupKey = groupKey;
        this.durationStart = durationStart;
    }

    public DataChangedEvent(ConfigGroupEnum groupKey, DataEventTypeEnum type, List<?> source) {
        super(source);
        this.eventType = type;
        this.groupKey = groupKey;
    }

    DataEventTypeEnum getEventType() {
        return eventType;
    }

    @Override
    public List<?> getSource() {
        return (List<?>) super.getSource();
    }

    public ConfigGroupEnum getGroupKey() {
        return this.groupKey;
    }

    public Long getDurationStart() {
        return durationStart;
    }
}
