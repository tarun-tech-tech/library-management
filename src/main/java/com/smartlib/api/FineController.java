package com.smartlib.api;

import com.smartlib.api.dto.FineResponse;
import com.smartlib.api.dto.PaymentRequest;
import com.smartlib.domain.AppUser;
import com.smartlib.service.FineService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fines")
public class FineController {

    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @GetMapping
    public List<FineResponse> list(@RequestParam(required = false) Long memberId,
                                   @AuthenticationPrincipal AppUser user) {
        return fineService.find(effectiveMemberId(memberId, user));
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public FineResponse payment(@PathVariable Long id, @Valid @RequestBody PaymentRequest request,
                                @AuthenticationPrincipal AppUser actor) {
        return fineService.recordPayment(id, request, actor);
    }

    private static Long effectiveMemberId(Long requestedMemberId, AppUser user) {
        if (!user.getRole().name().equals("MEMBER")) return requestedMemberId;
        if (user.getMember() == null) {
            throw new com.smartlib.service.ForbiddenOperationException("The account is not linked to a library member");
        }
        Long ownMemberId = user.getMember().getId();
        if (requestedMemberId != null && !requestedMemberId.equals(ownMemberId)) {
            throw new com.smartlib.service.ForbiddenOperationException("Members can only view their own fines");
        }
        return ownMemberId;
    }
}
