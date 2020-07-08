package com.ishansong.diablo.plugin.disruptor;

import com.lmax.disruptor.EventFactory;

public class DiabloEventFactory implements EventFactory<DiabloDataEvent> {

    @Override
    public DiabloDataEvent newInstance() {
        return new DiabloDataEvent();
    }
}
