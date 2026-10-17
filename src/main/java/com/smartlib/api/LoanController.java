package com.smartlib.api;

import com.smartlib.api.dto.IssueLoanRequest;
import com.smartlib.api.dto.LoanResponse;
import com.smartlib.api.dto.RenewLoanRequest;
import com.smartlib.domain.AppUser;
import com.smartlib.domain.LoanStatus;
import com.smartlib.service.ForbiddenOperationException;
import com.smartlib.service.LoanService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public List<LoanResponse> list(@RequestParam(required = false) Long memberId,
                                   @RequestParam(required = false) LoanStatus status,
                                   @AuthenticationPrincipal AppUser user) {
        return loanService.find(effectiveMemberId(memberId, user), status);
    }

    @GetMapping("/{id}")
    public LoanResponse get(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        LoanResponse response = loanService.get(id);
        if (user.getRole().name().equals("MEMBER") && !response.memberId().equals(effectiveMemberId(null, user))) {
            throw new ForbiddenOperationException("Members can only view their own loans");
        }
        return response;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public LoanResponse issue(@Valid @RequestBody IssueLoanRequest request) {
        return loanService.issue(request);
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public LoanResponse returnLoan(@PathVariable Long id) {
        return loanService.returnLoan(id);
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public LoanResponse renew(@PathVariable Long id, @Valid @RequestBody(required = false) RenewLoanRequest request) {
        return loanService.renew(id, request);
    }

    private static Long effectiveMemberId(Long requestedMemberId, AppUser user) {
        if (!user.getRole().name().equals("MEMBER")) return requestedMemberId;
        if (user.getMember() == null) {
            throw new ForbiddenOperationException("The account is not linked to a library member");
        }
        Long ownMemberId = user.getMember().getId();
        if (requestedMemberId != null && !requestedMemberId.equals(ownMemberId)) {
            throw new ForbiddenOperationException("Members can only view their own loans");
        }
        return ownMemberId;
    }
}
