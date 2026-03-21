package com.example.meteo.controller;

import com.example.meteo.model.ActualWeather;
import com.example.meteo.repository.ActualWeatherRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class TestController {

    private final ActualWeatherRepository repository;

    // Spring automatically gives us the repository here
    public TestController(ActualWeatherRepository repository) {
        this.repository = repository;
    }

    // These inner records act as a "mold" to catch the JSON data from the API
    record HourlyData(List<String> time, List<Double> temperature_2m) {}
    record OpenMeteoResponse(HourlyData hourly) {}

    @GetMapping("/api/test-fetch")
    public String fetchAndSave() {
        // 1. Call the Open-Meteo API for Łódź (Jan 1st, 2024)
        String url = "https://archive-api.open-meteo.com/v1/archive?latitude=51.75&longitude=19.46&start_date=2024-01-01&end_date=2024-01-01&hourly=temperature_2m";
        RestTemplate restTemplate = new RestTemplate();
        OpenMeteoResponse response = restTemplate.getForObject(url, OpenMeteoResponse.class);

        if (response != null && response.hourly() != null) {
            String timeString = response.hourly().time().get(0);
            Double temp = response.hourly().temperature_2m().get(0);

            ActualWeather weather = new ActualWeather();
            weather.setCity("Lodz");
            weather.setMeasuredAt(LocalDateTime.parse(timeString));
            weather.setTemperatureCelsius(temp);

            repository.save(weather);

            return "Success! Saved Łódź temperature: " + temp + "°C at " + timeString + " to the database.";
        }
        return "Failed to fetch data.";
    }
}