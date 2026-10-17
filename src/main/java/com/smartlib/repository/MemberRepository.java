package com.smartlib.repository;

import com.smartlib.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    long countByActiveTrue();
    List<Member> findAllByOrderByFullNameAsc();
}
