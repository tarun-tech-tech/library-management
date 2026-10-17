package com.smartlib.api;

import com.smartlib.api.dto.BookCopyResponse;
import com.smartlib.api.dto.BookCopyStatusRequest;
import com.smartlib.service.BookCopyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/book-copies")
public class BookCopyController {

    private final BookCopyService bookCopyService;

    public BookCopyController(BookCopyService bookCopyService) {
        this.bookCopyService = bookCopyService;
    }

    @GetMapping
    public List<BookCopyResponse> list(@RequestParam(required = false) Long bookId) {
        return bookCopyService.find(bookId).stream().map(ResponseMapper::copy).toList();
    }

    @GetMapping("/{id}")
    public BookCopyResponse get(@PathVariable Long id) {
        return ResponseMapper.copy(bookCopyService.get(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public BookCopyResponse changeStatus(@PathVariable Long id,
                                         @Valid @RequestBody BookCopyStatusRequest request) {
        return ResponseMapper.copy(bookCopyService.changeStatus(id, request));
    }
}
