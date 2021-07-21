package net.querybuilder4j.controller.health_check;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    public final static String MESSAGE = "I am healthy";

    @GetMapping("/health")
    public ResponseEntity<String> getHealthCheck() {
        return ResponseEntity.ok(MESSAGE);
    }

}
