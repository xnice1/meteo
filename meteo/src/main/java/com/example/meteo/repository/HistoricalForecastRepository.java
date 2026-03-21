package com.example.meteo.repository;

import com.example.meteo.model.HistoricalForecast;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricalForecastRepository extends JpaRepository<HistoricalForecast, Long> {
}