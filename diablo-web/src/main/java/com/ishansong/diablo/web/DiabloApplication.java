package com.ishansong.diablo.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.netty.ReactorNetty;

@SpringBootApplication(scanBasePackages = {"com.ishansong.diablo"})
public class DiabloApplication {

    private static final String PROPERTY_NAME_ASYNC_EVENT_ROUTER = "log4j2.AsyncQueueFullPolicy";
    private static final String PROPERTY_VALUE_DISCARDING_ASYNC_EVENT_ROUTER = "Discard";

    public static void main(String[] args) {

        System.setProperty(PROPERTY_NAME_ASYNC_EVENT_ROUTER, System.getProperty(PROPERTY_NAME_ASYNC_EVENT_ROUTER, PROPERTY_VALUE_DISCARDING_ASYNC_EVENT_ROUTER));
        System.setProperty(ReactorNetty.IO_WORKER_COUNT, System.getProperty(ReactorNetty.IO_WORKER_COUNT, "16"));
        System.setProperty(ReactorNetty.IO_SELECT_COUNT, System.getProperty(ReactorNetty.IO_SELECT_COUNT, "1"));

        SpringApplication.run(DiabloApplication.class, args);
    }
}
