package com.smartlib.service;

import com.smartlib.SmartLibProperties;
import com.smartlib.api.ResponseMapper;
import com.smartlib.api.dto.IssueLoanRequest;
import com.smartlib.api.dto.LoanResponse;
import com.smartlib.api.dto.RenewLoanRequest;
import com.smartlib.domain.BookCopy;
import com.smartlib.domain.CopyStatus;
import com.smartlib.domain.Fine;
import com.smartlib.domain.Loan;
import com.smartlib.domain.LoanStatus;
import com.smartlib.domain.Member;
import com.smartlib.domain.Book;
import com.smartlib.repository.BookRepository;
import com.smartlib.repository.BookCopyRepository;
import com.smartlib.repository.FineRepository;
import com.smartlib.repository.LoanRepository;
import com.smartlib.repository.MemberRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final FineRepository fineRepository;
    private final FineCalculationService fineCalculationService;
    private final SmartLibProperties properties;

    public LoanService(LoanRepository loanRepository, BookCopyRepository bookCopyRepository,
                       BookRepository bookRepository,
                       MemberRepository memberRepository, FineRepository fineRepository,
                       FineCalculationService fineCalculationService, SmartLibProperties properties) {
        this.loanRepository = loanRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.fineRepository = fineRepository;
        this.fineCalculationService = fineCalculationService;
        this.properties = properties;
    }

    @Transactional
    public LoanResponse issue(IssueLoanRequest request) {
        BookCopy copy = bookCopyRepository.findByIdForUpdate(request.bookCopyId())
                .orElseThrow(() -> new ResourceNotFoundException("Book copy " + request.bookCopyId() + " was not found"));
        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            throw new ConflictException("Book copy is not available for issue");
        }
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member " + request.memberId() + " was not found"));
        if (!member.isActive()) throw new ConflictException("Member is not active");

        LocalDate issueDate = LocalDate.now();
        int duration = request.durationDays() == null
                ? properties.getLoans().getDefaultDurationDays() : request.durationDays();
        Loan loan = new Loan(copy, member, issueDate, issueDate.plusDays(duration));
        copy.setStatus(CopyStatus.ISSUED);
        Book book = bookRepository.findByIdForUpdate(copy.getBook().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Book was not found for copy " + copy.getId()));
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        loanRepository.save(loan);
        return response(loan);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> find(Long memberId, LoanStatus status) {
        List<Loan> loans;
        if (memberId != null && status != null) {
            loans = loanRepository.findByMemberIdAndStatusOrderByIssueDateDesc(memberId, status);
        } else if (memberId != null) {
            loans = loanRepository.findByMemberIdOrderByIssueDateDesc(memberId);
        } else {
            loans = loanRepository.findAllByOrderByIssueDateDesc();
        }
        return loans.stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public LoanResponse get(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan " + id + " was not found"));
        return response(loan);
    }

    @Transactional
    public LoanResponse renew(Long id, RenewLoanRequest request) {
        Loan loan = loanRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan " + id + " was not found"));
        if (!loan.isIssued()) throw new ConflictException("Only issued loans can be renewed");
        if (loan.getRenewalCount() >= properties.getLoans().getMaxRenewals()) {
            throw new ConflictException("The maximum number of renewals has been reached");
        }
        if (loan.isOverdue(LocalDate.now())) throw new ConflictException("Overdue loans cannot be renewed");
        int additionalDays = request == null || request.additionalDays() == null
                ? properties.getLoans().getDefaultDurationDays() : request.additionalDays();
        loan.renew(loan.getDueDate().plusDays(additionalDays));
        return response(loan);
    }

    @Transactional
    public LoanResponse returnLoan(Long id) {
        Loan loan = loanRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan " + id + " was not found"));
        if (!loan.isIssued()) throw new ConflictException("Loan has already been returned");
        BookCopy copy = bookCopyRepository.findByIdForUpdate(loan.getBookCopy().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Book copy was not found for loan " + id));
        Book book = bookRepository.findByIdForUpdate(copy.getBook().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Book was not found for loan " + id));
        LocalDate returnedAt = LocalDate.now();
        FineCalculationService.FineResult fineResult = fineCalculationService.calculate(loan.getDueDate(), returnedAt);
        loan.markReturned(returnedAt);
        copy.setStatus(CopyStatus.AVAILABLE);
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        if (fineResult.amount().compareTo(BigDecimal.ZERO) > 0) {
            fineRepository.save(new Fine(loan, fineResult.amount(), fineResult.daysOverdue()));
        }
        return response(loan);
    }

    private LoanResponse response(Loan loan) {
        Fine fine = fineRepository.findByLoanId(loan.getId()).orElse(null);
        BigDecimal fineAmount = fine == null
                ? fineCalculationService.calculate(loan.getDueDate(), LocalDate.now()).amount()
                : fine.getAmount();
        int overdueDays = fine == null
                ? ResponseMapper.overdueDays(loan, LocalDate.now()) : fine.getDaysOverdue();
        return ResponseMapper.loan(loan, fineAmount, overdueDays);
    }
}
