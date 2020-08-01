package com.ishansong.diablo.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.ishansong.diablo"})
public class DiabloAdminApplication {

    public static void main(final String[] args) {
        SpringApplication.run(DiabloAdminApplication.class, args);
    }
}

