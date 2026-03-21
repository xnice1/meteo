package com.example.meteo.repository;

import com.example.meteo.model.ActualWeather;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActualWeatherRepository extends JpaRepository<ActualWeather, Long> {
}