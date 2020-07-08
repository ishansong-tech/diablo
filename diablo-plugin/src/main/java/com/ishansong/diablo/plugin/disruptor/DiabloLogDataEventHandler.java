package com.ishansong.diablo.plugin.disruptor;

import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public class DiabloLogDataEventHandler implements EventHandler<DiabloDataEvent> {

    private static final Logger logger= LoggerFactory.getLogger(DiabloLogDataEventHandler.class);

    private Executor executor;

    public DiabloLogDataEventHandler(Executor executor){
        this.executor=executor;
    }

    @Override
    public void onEvent(DiabloDataEvent diabloDataEvent, long l, boolean b) throws Exception {
        diabloDataEvent.clear();
    }
}
