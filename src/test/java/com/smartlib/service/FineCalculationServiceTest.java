package com.smartlib.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.smartlib.SmartLibProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class FineCalculationServiceTest {

    @Test
    void doesNotChargeForAnOnTimeReturn() {
        SmartLibProperties properties = new SmartLibProperties();
        properties.getLoans().setFinePerDay(new BigDecimal("1.50"));

        FineCalculationService.FineResult result = new FineCalculationService(properties)
                .calculate(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 10));

        assertEquals(0, result.daysOverdue());
        assertEquals(new BigDecimal("0.00"), result.amount());
    }

    @Test
    void calculatesAcrossMonthBoundariesWithDecimalCurrency() {
        SmartLibProperties properties = new SmartLibProperties();
        properties.getLoans().setFinePerDay(new BigDecimal("1.50"));

        FineCalculationService.FineResult result = new FineCalculationService(properties)
                .calculate(LocalDate.of(2026, 1, 30), LocalDate.of(2026, 2, 2));

        assertEquals(3, result.daysOverdue());
        assertEquals(new BigDecimal("4.50"), result.amount());
    }
}
