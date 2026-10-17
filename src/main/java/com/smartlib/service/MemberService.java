package com.smartlib.service;

import com.smartlib.api.dto.MemberRequest;
import com.smartlib.domain.Member;
import com.smartlib.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<Member> findAll() { return memberRepository.findAllByOrderByFullNameAsc(); }

    @Transactional(readOnly = true)
    public Member get(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member " + id + " was not found"));
    }

    @Transactional
    public Member create(MemberRequest request) {
        if (memberRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("A member with that email already exists");
        }
        try {
            return memberRepository.save(new Member(request.fullName().trim(), request.email().trim().toLowerCase(),
                    trimToNull(request.phone()), trimToNull(request.address()), LocalDate.now()));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("A member with that email already exists");
        }
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
