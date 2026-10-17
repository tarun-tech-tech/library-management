package com.smartlib.service;

import com.smartlib.SmartLibProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;

@Service
public class FineCalculationService {

    private final SmartLibProperties properties;

    public FineCalculationService(SmartLibProperties properties) {
        this.properties = properties;
    }

    public FineResult calculate(LocalDate dueDate, LocalDate endDate) {
        int daysOverdue = dueDate.isBefore(endDate) ? Math.toIntExact(ChronoUnit.DAYS.between(dueDate, endDate)) : 0;
        BigDecimal amount = properties.getLoans().getFinePerDay()
                .multiply(BigDecimal.valueOf(daysOverdue))
                .setScale(2, RoundingMode.HALF_UP);
        return new FineResult(daysOverdue, amount);
    }

    public record FineResult(int daysOverdue, BigDecimal amount) {
    }
}
