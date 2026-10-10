package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {
    @GetMapping("/")
    public Map<String, String> index() {
        return Map.of(
                "message", "Diary REST API server is running.",
                "diaries", "/api/diaries"
        );
    }
}
