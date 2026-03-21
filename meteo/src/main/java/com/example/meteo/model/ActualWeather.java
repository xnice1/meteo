package com.example.meteo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "actual_weather")
public class ActualWeather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;
    private LocalDateTime measuredAt;
    private Double temperatureCelsius;
    private Double precipitationMm;
}