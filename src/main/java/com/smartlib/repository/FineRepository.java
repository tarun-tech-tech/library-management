package com.smartlib.repository;

import com.smartlib.domain.Fine;
import com.smartlib.domain.FineStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FineRepository extends JpaRepository<Fine, Long> {
    @EntityGraph(attributePaths = {"loan", "loan.bookCopy", "loan.bookCopy.book", "loan.member"})
    List<Fine> findAllByOrderByAssessedAtDesc();

    @EntityGraph(attributePaths = {"loan", "loan.bookCopy", "loan.bookCopy.book", "loan.member"})
    List<Fine> findByLoanMemberIdOrderByAssessedAtDesc(Long memberId);

    Optional<Fine> findByLoanId(Long loanId);
    long countByStatus(FineStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Fine f join fetch f.loan l join fetch l.member where f.id = :id")
    Optional<Fine> findByIdForUpdate(@Param("id") Long id);
}
