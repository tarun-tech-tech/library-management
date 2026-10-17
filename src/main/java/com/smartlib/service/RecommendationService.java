package com.smartlib.service;

import com.smartlib.api.dto.RecommendationResponse;
import com.smartlib.domain.Book;
import com.smartlib.domain.Loan;
import com.smartlib.domain.Member;
import com.smartlib.domain.Recommendation;
import com.smartlib.repository.BookRepository;
import com.smartlib.repository.LoanRepository;
import com.smartlib.repository.MemberRepository;
import com.smartlib.repository.RecommendationRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final MemberRepository memberRepository;
    private final RecommendationRepository recommendationRepository;

    public RecommendationService(BookRepository bookRepository, LoanRepository loanRepository,
                                 MemberRepository memberRepository, RecommendationRepository recommendationRepository) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.memberRepository = memberRepository;
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional
    public List<RecommendationResponse> recommend(Long memberId) {
        Member member = memberId == null ? null : memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member " + memberId + " was not found"));
        List<Loan> history = member == null ? List.of() : loanRepository.findByMemberIdOrderByIssueDateDesc(memberId);
        Map<String, Integer> categoryWeights = new HashMap<>();
        Map<String, Integer> authorWeights = new HashMap<>();
        for (Loan loan : history) {
            categoryWeights.merge(loan.getBookCopy().getBook().getCategory(), 1, Integer::sum);
            authorWeights.merge(loan.getBookCopy().getBook().getAuthor(), 1, Integer::sum);
        }

        List<ScoredBook> scored = bookRepository.findAllByOrderByTitleAsc().stream()
                .map(book -> score(book, categoryWeights, authorWeights))
                .filter(item -> item.book().getAvailableCopies() > 0)
                .sorted(Comparator.comparing(ScoredBook::score).reversed()
                        .thenComparing(item -> item.book().getTitle(), String.CASE_INSENSITIVE_ORDER))
                .limit(10)
                .toList();

        List<Recommendation> snapshots = scored.stream()
                .map(item -> new Recommendation(member, item.book(), item.score(), item.reason()))
                .toList();
        recommendationRepository.saveAll(snapshots);
        return scored.stream().map(item -> new RecommendationResponse(item.book().getId(), item.book().getTitle(),
                item.book().getAuthor(), item.book().getCategory(), item.book().getAvailableCopies(),
                item.score(), item.reason())).toList();
    }

    private ScoredBook score(Book book, Map<String, Integer> categoryWeights, Map<String, Integer> authorWeights) {
        int categoryScore = categoryWeights.getOrDefault(book.getCategory(), 0);
        int authorScore = authorWeights.getOrDefault(book.getAuthor(), 0);
        BigDecimal score = BigDecimal.valueOf(categoryScore * 3L + authorScore * 2L)
                .add(book.getAvailableCopies() > 0 ? BigDecimal.ONE : BigDecimal.ZERO)
                .setScale(4, RoundingMode.HALF_UP);
        String reason = categoryScore > 0
                ? "Matches a category from this member's loan history"
                : authorScore > 0
                    ? "Matches an author from this member's loan history"
                    : "Available in the current SmartLib catalog";
        return new ScoredBook(book, score, reason);
    }

    private record ScoredBook(Book book, BigDecimal score, String reason) {
    }
}
