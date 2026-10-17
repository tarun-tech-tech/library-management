package com.smartlib.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FinePaymentTest {

    @Test
    void supportsPartialPaymentAndClosesTheBalance() {
        Fine fine = new Fine(null, new BigDecimal("30.00"), 6);

        fine.recordPayment(new BigDecimal("12.50"));
        assertEquals(FineStatus.PARTIALLY_PAID, fine.getStatus());
        assertEquals(new BigDecimal("12.50"), fine.getAmountPaid());
        assertEquals(new BigDecimal("17.50"), fine.getBalance());

        fine.recordPayment(new BigDecimal("17.50"));
        assertEquals(FineStatus.PAID, fine.getStatus());
        assertEquals(BigDecimal.ZERO.setScale(2), fine.getBalance());
    }

    @Test
    void rejectsPaymentAboveTheOutstandingBalance() {
        Fine fine = new Fine(null, new BigDecimal("30.00"), 6);

        assertThrows(IllegalArgumentException.class,
                () -> fine.recordPayment(new BigDecimal("30.01")));
    }
}
