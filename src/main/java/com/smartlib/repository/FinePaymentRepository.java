package com.smartlib.repository;

import com.smartlib.domain.FinePayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinePaymentRepository extends JpaRepository<FinePayment, Long> {
}
