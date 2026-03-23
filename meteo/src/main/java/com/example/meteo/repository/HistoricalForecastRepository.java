package com.example.meteo.repository;

import com.example.meteo.model.HistoricalForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface HistoricalForecastRepository extends JpaRepository<HistoricalForecast, Long> {
    @Query(value = """
        SELECT
            f.target_date AS date,
            f.predicted_temperature_celsius AS guessed_temp,
            a.temperature_celsius AS actual_temp,
            ABS(f.predicted_temperature_celsius - a.temperature_celsius) AS error_margin
        FROM historical_forecasts f
        JOIN actual_weather a
          ON f.target_date = a.measured_at AND f.city = a.city
        WHERE f.provider_name = :providerName
        ORDER BY f.target_date ASC
    """, nativeQuery = true)
    List<Object[]> calculateAccuracyForModel(@Param("providerName") String providerName);
}