package com.pfe.backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class ActuatorRedirectController {

    @GetMapping("/actuator/")
    public ResponseEntity<Void> redirectTrailingSlash() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/actuator"));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}

