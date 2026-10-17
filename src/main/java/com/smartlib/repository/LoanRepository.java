package com.smartlib.repository;

import com.smartlib.domain.Loan;
import com.smartlib.domain.LoanStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    @EntityGraph(attributePaths = {"bookCopy", "bookCopy.book", "member"})
    List<Loan> findAllByOrderByIssueDateDesc();

    @EntityGraph(attributePaths = {"bookCopy", "bookCopy.book", "member"})
    List<Loan> findByMemberIdOrderByIssueDateDesc(Long memberId);

    @EntityGraph(attributePaths = {"bookCopy", "bookCopy.book", "member"})
    List<Loan> findByMemberIdAndStatusOrderByIssueDateDesc(Long memberId, LoanStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Loan l join fetch l.bookCopy c join fetch c.book join fetch l.member where l.id = :id")
    Optional<Loan> findByIdForUpdate(@Param("id") Long id);
}
