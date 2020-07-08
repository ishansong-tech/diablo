package com.ishansong.diablo.web.controller;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class RouteController {

    @GetMapping("/ping")
    public Mono<String> all() {
        return Mono.just("pong");
    }

}
