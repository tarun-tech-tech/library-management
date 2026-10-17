package com.smartlib.service;

import com.smartlib.api.ResponseMapper;
import com.smartlib.api.dto.FineResponse;
import com.smartlib.api.dto.PaymentRequest;
import com.smartlib.domain.AppUser;
import com.smartlib.domain.Fine;
import com.smartlib.domain.FinePayment;
import com.smartlib.repository.FineRepository;
import com.smartlib.repository.FinePaymentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FineService {

    private final FineRepository fineRepository;
    private final FinePaymentRepository finePaymentRepository;

    public FineService(FineRepository fineRepository, FinePaymentRepository finePaymentRepository) {
        this.fineRepository = fineRepository;
        this.finePaymentRepository = finePaymentRepository;
    }

    @Transactional(readOnly = true)
    public List<FineResponse> find(Long memberId) {
        List<Fine> fines = memberId == null
                ? fineRepository.findAllByOrderByAssessedAtDesc()
                : fineRepository.findByLoanMemberIdOrderByAssessedAtDesc(memberId);
        return fines.stream().map(ResponseMapper::fine).toList();
    }

    @Transactional
    public FineResponse recordPayment(Long id, PaymentRequest request, AppUser actor) {
        Fine fine = fineRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fine " + id + " was not found"));
        fine.recordPayment(request.amount());
        finePaymentRepository.save(new FinePayment(fine, request.amount(), request.paymentMethod(),
                request.referenceNumber() == null || request.referenceNumber().isBlank()
                        ? null : request.referenceNumber().trim(), actor));
        return ResponseMapper.fine(fine);
    }
}
