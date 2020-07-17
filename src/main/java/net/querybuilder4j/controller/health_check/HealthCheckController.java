package net.querybuilder4j.controller.health_check;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HealthCheckController {

    @GetMapping("/")
    public ResponseEntity<String> getHealthCheck() {
        return ResponseEntity.ok("I am healthy");
    }

}
