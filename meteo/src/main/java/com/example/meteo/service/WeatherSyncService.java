package com.example.meteo.service;

import com.example.meteo.model.ActualWeather;
import com.example.meteo.model.HistoricalForecast;
import com.example.meteo.repository.ActualWeatherRepository;
import com.example.meteo.repository.HistoricalForecastRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WeatherSyncService {

    private final ActualWeatherRepository actualWeatherRepository;
    private final HistoricalForecastRepository forecastRepository;

    public WeatherSyncService(ActualWeatherRepository actualWeatherRepository, HistoricalForecastRepository forecastRepository) {
        this.actualWeatherRepository = actualWeatherRepository;
        this.forecastRepository = forecastRepository;
    }

    record ActualHourlyData(List<String> time, List<Double> temperature_2m) {}
    record ActualResponse(ActualHourlyData hourly) {}

    public String fetchAndSaveActualWeather() {
        String url = "https://archive-api.open-meteo.com/v1/archive?latitude=51.75&longitude=19.46&start_date=2024-01-01&end_date=2024-01-31&hourly=temperature_2m";
        RestTemplate restTemplate = new RestTemplate();
        ActualResponse response = restTemplate.getForObject(url, ActualResponse.class);

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
        return "Failed to fetch actual data.";
    }

    record ForecastHourlyData(List<String> time, List<Double> temperature_2m_gfs_seamless, List<Double> temperature_2m_icon_seamless) {}
    record ForecastResponse(ForecastHourlyData hourly) {}

    public String fetchAndSaveForecasts() {
        String url = "https://historical-forecast-api.open-meteo.com/v1/forecast?latitude=51.75&longitude=19.46&start_date=2024-01-01&end_date=2024-01-31&hourly=temperature_2m&models=gfs_seamless,icon_seamless";
        RestTemplate restTemplate = new RestTemplate();
        ForecastResponse response = restTemplate.getForObject(url, ForecastResponse.class);

        if (response != null && response.hourly() != null) {
            int savedCount = 0;

            for (int i = 0; i < response.hourly().time().size(); i++) {
                String timeString = response.hourly().time().get(i);
                LocalDateTime targetDate = LocalDateTime.parse(timeString);

                Double gfsTemp = response.hourly().temperature_2m_gfs_seamless().get(i);
                if (gfsTemp != null) {
                    HistoricalForecast gfsForecast = new HistoricalForecast();
                    gfsForecast.setCity("Lodz");
                    gfsForecast.setProviderName("GFS");
                    gfsForecast.setTargetDate(targetDate);
                    gfsForecast.setDaysInAdvance(1);
                    gfsForecast.setPredictedTemperatureCelsius(gfsTemp);
                    forecastRepository.save(gfsForecast);
                    savedCount++;
                }

                Double iconTemp = response.hourly().temperature_2m_icon_seamless().get(i);
                if (iconTemp != null) {
                    HistoricalForecast iconForecast = new HistoricalForecast();
                    iconForecast.setCity("Lodz");
                    iconForecast.setProviderName("ICON"); // Call it ICON now!
                    iconForecast.setTargetDate(targetDate);
                    iconForecast.setDaysInAdvance(1);
                    iconForecast.setPredictedTemperatureCelsius(iconTemp);
                    forecastRepository.save(iconForecast);
                    savedCount++;
                }
            }
            return "Success! Saved " + savedCount + " forecast records (GFS & ECMWF) for Łódź.";
        }
        return "Failed to fetch forecast data.";
    }

    public String calculateProviderWinPercentage() {
        List<Object[]> gfsResults = forecastRepository.calculateAccuracyForModel("GFS");
        List<Object[]> iconResults = forecastRepository.calculateAccuracyForModel("ICON"); // Changed to ICON

        double tolerance = 2.0;

        double gfsPercentage = calculatePercentage(gfsResults, tolerance);
        double iconPercentage = calculatePercentage(iconResults, tolerance); // Changed to ICON

        return "<h1>Thesis Accuracy Results (January 2024)</h1>" +
                "<b>Rule:</b> A forecast is 'exact' if it is within +/- " + tolerance + "°C<br><br>" +
                "<h2>GFS Accuracy: " + String.format("%.2f", gfsPercentage) + "%</h2>" +
                "<h2>ICON Accuracy: " + String.format("%.2f", iconPercentage) + "%</h2>"; // Changed to ICON
    }

    private double calculatePercentage(List<Object[]> results, double tolerance) {
        if (results.isEmpty()) return 0.0;

        int exactMatchCount = 0;
        for (Object[] row : results) {
            double error = ((Number) row[3]).doubleValue();
            if (error <= tolerance) {
                exactMatchCount++;
            }
        }
        return ((double) exactMatchCount / results.size()) * 100.0;
    }
}