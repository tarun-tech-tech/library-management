package com.smartlib.api;

import com.smartlib.api.dto.RecommendationResponse;
import com.smartlib.domain.AppUser;
import com.smartlib.service.ForbiddenOperationException;
import com.smartlib.service.RecommendationService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public List<RecommendationResponse> list(@RequestParam(required = false) Long memberId,
                                              @AuthenticationPrincipal AppUser user) {
        if (user.getRole().name().equals("MEMBER")) {
            if (user.getMember() == null) {
                return recommendationService.recommend(null);
            }
            if (memberId != null && !memberId.equals(user.getMember().getId())) {
                throw new ForbiddenOperationException("Members can only request their own recommendations");
            }
            return recommendationService.recommend(user.getMember().getId());
        }
        return recommendationService.recommend(memberId);
    }
}
