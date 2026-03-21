package com.example.meteo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "historical_forecasts")
public class HistoricalForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;
    private String providerName;

    private LocalDateTime targetDate;

    private Integer daysInAdvance;

    private Double predictedTemperatureCelsius;
}