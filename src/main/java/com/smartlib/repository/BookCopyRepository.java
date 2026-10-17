package com.smartlib.repository;

import com.smartlib.domain.BookCopy;
import com.smartlib.domain.CopyStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    @EntityGraph(attributePaths = "book")
    List<BookCopy> findAllByOrderByInventoryCodeAsc();

    @EntityGraph(attributePaths = "book")
    List<BookCopy> findByBookIdOrderByInventoryCodeAsc(Long bookId);

    @EntityGraph(attributePaths = "book")
    Optional<BookCopy> findWithBookById(Long id);

    long countByBookIdAndStatus(Long bookId, CopyStatus status);
    long countByStatus(CopyStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from BookCopy c join fetch c.book where c.id = :id")
    Optional<BookCopy> findByIdForUpdate(@Param("id") Long id);
}
