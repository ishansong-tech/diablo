package com.ishansong.diablo.demo.api.controller;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class HealthController {

    private volatile boolean started = false;

    @RequestMapping("/health")
    public void index(@RequestParam(value = "enable", required = false) Boolean enable, HttpServletResponse response) {

        if (enable != null) {
            started = enable;

            return;
        }

        if (!started) {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initAfterStartup() {

        try {
            Thread.sleep(3000);

            started = true;
        } catch (InterruptedException e) {
            // ingore
        }

    }

}
