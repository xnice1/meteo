package com.example.meteo.service;

import com.example.meteo.model.ActualWeather;
import com.example.meteo.repository.ActualWeatherRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WeatherSyncService {

    private final ActualWeatherRepository actualWeatherRepository;

    public WeatherSyncService(ActualWeatherRepository actualWeatherRepository) {
        this.actualWeatherRepository = actualWeatherRepository;
    }

    record HourlyData(List<String> time, List<Double> temperature_2m) {}
    record OpenMeteoResponse(HourlyData hourly) {}

    public String fetchAndSaveActualWeather() {
        String url = "https://archive-api.open-meteo.com/v1/archive?latitude=51.75&longitude=19.46&start_date=2024-01-01&end_date=2024-01-01&hourly=temperature_2m";
        RestTemplate restTemplate = new RestTemplate();
        OpenMeteoResponse response = restTemplate.getForObject(url, OpenMeteoResponse.class);

        if (response != null && response.hourly() != null) {
            int savedCount = 0;

            for (int i = 0; i < response.hourly().time().size(); i++) {
                ActualWeather weather = new ActualWeather();
                weather.setCity("Lodz");
                weather.setMeasuredAt(LocalDateTime.parse(response.hourly().time().get(i)));
                weather.setTemperatureCelsius(response.hourly().temperature_2m().get(i));

                actualWeatherRepository.save(weather);
                savedCount++;
            }
            return "Success! Saved " + savedCount + " hours of actual weather data for Łódź.";
        }
        return "Failed to fetch data.";
    }
}