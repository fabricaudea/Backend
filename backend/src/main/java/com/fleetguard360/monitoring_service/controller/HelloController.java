package com.fleetguard360.monitoring_service.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("!demo")
public class HelloController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello World desde FleetGuard360 🚀";
    }
}