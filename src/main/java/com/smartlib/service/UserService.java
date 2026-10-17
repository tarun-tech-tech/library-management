package com.smartlib.service;

import com.smartlib.api.dto.UserCreateRequest;
import com.smartlib.domain.AppUser;
import com.smartlib.domain.Member;
import com.smartlib.repository.MemberRepository;
import com.smartlib.repository.UserRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AppUser> findAll() { return userRepository.findAllUsers(); }

    @Transactional
    public AppUser create(UserCreateRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ConflictException("A user with that username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("A user with that email already exists");
        }
        Member member = null;
        if (request.memberId() != null) {
            member = memberRepository.findById(request.memberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member " + request.memberId() + " was not found"));
        }
        AppUser user = new AppUser(request.username().trim(), passwordEncoder.encode(request.password()),
                request.fullName().trim(), request.email().trim().toLowerCase(), request.role());
        user.setMember(member);
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Username or email is already in use");
        }
    }
}
