package com.smartlib.web;

import com.smartlib.api.ResponseMapper;
import com.smartlib.api.dto.RecommendationResponse;
import com.smartlib.domain.AppUser;
import com.smartlib.service.BookService;
import com.smartlib.service.LoanService;
import com.smartlib.service.FineService;
import com.smartlib.service.RecommendationService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    private final DashboardService dashboardService;
    private final BookService bookService;
    private final LoanService loanService;
    private final FineService fineService;
    private final RecommendationService recommendationService;

    public WebController(DashboardService dashboardService, BookService bookService,
                         LoanService loanService, FineService fineService, RecommendationService recommendationService) {
        this.dashboardService = dashboardService;
        this.bookService = bookService;
        this.loanService = loanService;
        this.fineService = fineService;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal AppUser user, Model model) {
        model.addAttribute("pageTitle", "Library intelligence");
        model.addAttribute("snapshot", dashboardService.snapshot());
        model.addAttribute("currentUser", user);
        return "dashboard/index";
    }

    @GetMapping("/catalog")
    public String catalog(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("pageTitle", "Catalog");
        model.addAttribute("query", q == null ? "" : q);
        model.addAttribute("books", bookService.find(q).stream().map(ResponseMapper::book).toList());
        return "catalog/index";
    }

    @GetMapping("/catalog/{id}")
    public String catalogDetail(@PathVariable Long id, Model model) {
        var book = bookService.get(id);
        model.addAttribute("pageTitle", book.getTitle());
        model.addAttribute("book", ResponseMapper.book(book));
        model.addAttribute("copies", bookService.copies(id).stream().map(ResponseMapper::copy).toList());
        return "catalog/detail";
    }

    @GetMapping("/circulation")
    public String circulation(Model model) {
        model.addAttribute("pageTitle", "Circulation");
        model.addAttribute("loans", loanService.find(null, null));
        return "circulation/index";
    }

    @GetMapping("/recommendations")
    public String recommendations(@AuthenticationPrincipal AppUser user, Model model) {
        Long memberId = user.getMember() == null ? null : user.getMember().getId();
        List<RecommendationResponse> recommendations = recommendationService.recommend(memberId);
        model.addAttribute("pageTitle", "Reading signals");
        model.addAttribute("recommendations", recommendations);
        return "recommendations/index";
    }

    @GetMapping("/fines")
    public String fines(@AuthenticationPrincipal AppUser user, Model model) {
        Long memberId = user.getMember() == null ? null : user.getMember().getId();
        model.addAttribute("pageTitle", "Fine ledger");
        model.addAttribute("fines", fineService.find(user.getRole().name().equals("MEMBER") ? memberId : null));
        model.addAttribute("canRecordPayment", !user.getRole().name().equals("MEMBER"));
        return "fines/index";
    }
}
