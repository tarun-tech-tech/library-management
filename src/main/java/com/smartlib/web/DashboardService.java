package com.smartlib.web;

import com.smartlib.api.ResponseMapper;
import com.smartlib.api.dto.BookResponse;
import com.smartlib.api.dto.LoanResponse;
import com.smartlib.domain.CopyStatus;
import com.smartlib.domain.Fine;
import com.smartlib.domain.FineStatus;
import com.smartlib.domain.Loan;
import com.smartlib.domain.LoanStatus;
import com.smartlib.repository.BookCopyRepository;
import com.smartlib.repository.BookRepository;
import com.smartlib.repository.FineRepository;
import com.smartlib.repository.LoanRepository;
import com.smartlib.repository.MemberRepository;
import com.smartlib.service.FineCalculationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final FineRepository fineRepository;
    private final FineCalculationService fineCalculationService;

    public DashboardService(BookRepository bookRepository, BookCopyRepository bookCopyRepository,
                            MemberRepository memberRepository, LoanRepository loanRepository,
                            FineRepository fineRepository, FineCalculationService fineCalculationService) {
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
        this.fineRepository = fineRepository;
        this.fineCalculationService = fineCalculationService;
    }

    @Transactional(readOnly = true)
    public DashboardSnapshot snapshot() {
        List<BookResponse> books = bookRepository.findAllByOrderByTitleAsc().stream()
                .map(ResponseMapper::book).toList();
        List<Loan> loanEntities = loanRepository.findAllByOrderByIssueDateDesc();
        List<LoanResponse> loans = loanEntities.stream().limit(8)
                .map(this::toResponse).toList();
        long activeLoans = loanEntities.stream().filter(loan -> loan.getStatus() == LoanStatus.ISSUED).count();
        long overdueLoans = loanEntities.stream()
                .filter(loan -> loan.isOverdue(LocalDate.now())).count();
        BigDecimal outstanding = fineRepository.findAllByOrderByAssessedAtDesc().stream()
                .filter(fine -> fine.getStatus() != FineStatus.PAID)
                .map(Fine::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardSnapshot(
                books.size(),
                bookCopyRepository.count(),
                bookCopyRepository.countByStatus(CopyStatus.AVAILABLE),
                memberRepository.countByActiveTrue(),
                activeLoans,
                overdueLoans,
                outstanding,
                books,
                loans);
    }

    private LoanResponse toResponse(Loan loan) {
        Fine fine = fineRepository.findByLoanId(loan.getId()).orElse(null);
        BigDecimal amount = fine == null
                ? fineCalculationService.calculate(loan.getDueDate(), LocalDate.now()).amount()
                : fine.getAmount();
        int overdueDays = fine == null
                ? ResponseMapper.overdueDays(loan, LocalDate.now()) : fine.getDaysOverdue();
        return ResponseMapper.loan(loan, amount, overdueDays);
    }
}
