package com.ishansong.diablo.plugin.disruptor;

import com.ishansong.diablo.core.model.access.AccessLog;
import com.lmax.disruptor.EventTranslatorOneArg;

import java.util.List;

public class DiabloEventTranslator implements EventTranslatorOneArg<DiabloDataEvent, List<AccessLog>> {

    @Override
    public void translateTo(final DiabloDataEvent diabloDataEvent, final long l, final List<AccessLog> accessLogs) {
        diabloDataEvent.setAccessLogs(accessLogs);
    }
}
