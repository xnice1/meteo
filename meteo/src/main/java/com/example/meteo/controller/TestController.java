package com.example.meteo.controller;

import com.example.meteo.service.WeatherSyncService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final WeatherSyncService weatherSyncService;

    public TestController(WeatherSyncService weatherSyncService) {
        this.weatherSyncService = weatherSyncService;
    }

    @GetMapping("/api/test-fetch")
    public String fetchAndSave() {
        return weatherSyncService.fetchAndSaveActualWeather();
    }

    @GetMapping("/api/test-forecasts")
    public String fetchForecasts() {
        return weatherSyncService.fetchAndSaveForecasts();
    }
}