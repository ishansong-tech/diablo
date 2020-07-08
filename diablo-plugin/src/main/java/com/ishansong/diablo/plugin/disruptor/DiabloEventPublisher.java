package com.ishansong.diablo.plugin.disruptor;

import com.ishansong.diablo.core.concurrent.DiabloThreadFactory;
import com.ishansong.diablo.core.model.access.AccessLog;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.IgnoreExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DiabloEventPublisher implements InitializingBean, DisposableBean {

    private Disruptor<DiabloDataEvent> disruptor;

    @Value("${diablo.disruptor.bufferSize:4096}")
    private int bufferSize;

    @Value("${diablo.disruptor.threadSize:1}")
    private int threadSize;

    public DiabloEventPublisher() {
    }

    private void start() {

        disruptor = new Disruptor<>(new DiabloEventFactory(), bufferSize, DiabloThreadFactory.create("monitor-disruptor-thread-", false), ProducerType.MULTI, new BlockingWaitStrategy());

        final Executor flushLogExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), DiabloThreadFactory.create("monitor-disruptor-executor", false), new ThreadPoolExecutor.AbortPolicy());
        disruptor.handleEventsWith(new DiabloLogDataEventHandler(flushLogExecutor));
        disruptor.setDefaultExceptionHandler(new IgnoreExceptionHandler());
        disruptor.start();
    }

    public void publishEvent(List<AccessLog> accessLogs) {
        final RingBuffer<DiabloDataEvent> ringBuffer = disruptor.getRingBuffer();
        ringBuffer.publishEvent(new DiabloEventTranslator(), accessLogs);

    }

    @Override
    public void destroy() {
        disruptor.shutdown();
    }

    @Override
    public void afterPropertiesSet() {
        //start();
    }
}
