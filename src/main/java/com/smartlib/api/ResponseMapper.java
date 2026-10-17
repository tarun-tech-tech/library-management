package com.smartlib.api;

import com.smartlib.api.dto.BookCopyResponse;
import com.smartlib.api.dto.BookResponse;
import com.smartlib.api.dto.FineResponse;
import com.smartlib.api.dto.LoanResponse;
import com.smartlib.api.dto.MemberResponse;
import com.smartlib.api.dto.UserResponse;
import com.smartlib.domain.AppUser;
import com.smartlib.domain.Book;
import com.smartlib.domain.BookCopy;
import com.smartlib.domain.Fine;
import com.smartlib.domain.Loan;
import com.smartlib.domain.Member;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class ResponseMapper {

    private ResponseMapper() {
    }

    public static BookResponse book(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(),
                book.getCategory(), book.getPublisher(), book.getPublicationYear(),
                book.getTotalCopies(), book.getAvailableCopies());
    }

    public static BookCopyResponse copy(BookCopy copy) {
        return new BookCopyResponse(copy.getId(), copy.getBook().getId(), copy.getInventoryCode(),
                copy.getStatus(), copy.getAcquiredAt());
    }

    public static MemberResponse member(Member member) {
        return new MemberResponse(member.getId(), member.getFullName(), member.getEmail(),
                member.getPhone(), member.getAddress(), member.getRegistrationDate(), member.isActive());
    }

    public static UserResponse user(AppUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(),
                user.getRole(), user.getMember() == null ? null : user.getMember().getId(), user.isEnabled());
    }

    public static LoanResponse loan(Loan loan, BigDecimal fineAmount, int overdueDays) {
        return new LoanResponse(loan.getId(), loan.getBookCopy().getBook().getId(),
                loan.getBookCopy().getBook().getTitle(), loan.getBookCopy().getId(),
                loan.getBookCopy().getInventoryCode(), loan.getMember().getId(), loan.getMember().getFullName(),
                loan.getIssueDate(), loan.getDueDate(), loan.getReturnedAt(), loan.getStatus(),
                loan.getRenewalCount(), fineAmount == null ? BigDecimal.ZERO : fineAmount, overdueDays);
    }

    public static FineResponse fine(Fine fine) {
        return new FineResponse(fine.getId(), fine.getLoan().getId(), fine.getLoan().getMember().getId(),
                fine.getLoan().getMember().getFullName(), fine.getAmount(), fine.getAmountPaid(), fine.getBalance(), fine.getDaysOverdue(),
                fine.getStatus(), fine.getAssessedAt(), fine.getPaidAt());
    }

    public static int overdueDays(Loan loan, LocalDate asOf) {
        LocalDate end = loan.getReturnedAt() == null ? asOf : loan.getReturnedAt();
        return end.isAfter(loan.getDueDate()) ? Math.toIntExact(ChronoUnit.DAYS.between(loan.getDueDate(), end)) : 0;
    }
}
