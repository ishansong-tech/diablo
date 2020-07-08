package com.ishansong.diablo.plugin.disruptor;

import com.ishansong.diablo.core.model.access.AccessLog;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DiabloDataEvent implements Serializable {

    private List<AccessLog> accessLogs;

    public void clear() {
        //help gc
        accessLogs.clear();
        accessLogs = null;
    }
}
