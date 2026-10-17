package com.smartlib.api;

import com.smartlib.api.dto.MemberRequest;
import com.smartlib.api.dto.MemberResponse;
import com.smartlib.service.MemberService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberResponse> list() {
        return memberService.findAll().stream().map(ResponseMapper::member).toList();
    }

    @GetMapping("/{id}")
    public MemberResponse get(@PathVariable Long id) {
        return ResponseMapper.member(memberService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse create(@Valid @RequestBody MemberRequest request) {
        return ResponseMapper.member(memberService.create(request));
    }
}
