package com.ishansong.diablo.admin.service;

import com.ishansong.diablo.core.enums.DataEventTypeEnum;

public interface SyncDataService {

    boolean syncAll(DataEventTypeEnum type);

    boolean syncPluginData(String pluginId);

}
