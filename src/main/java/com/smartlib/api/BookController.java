package com.smartlib.api;

import com.smartlib.api.dto.BookCopyResponse;
import com.smartlib.api.dto.BookRequest;
import com.smartlib.api.dto.BookResponse;
import com.smartlib.service.BookService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookResponse> list(@RequestParam(required = false) String q) {
        return bookService.find(q).stream().map(ResponseMapper::book).toList();
    }

    @GetMapping("/{id}")
    public BookResponse get(@PathVariable Long id) {
        return ResponseMapper.book(bookService.get(id));
    }

    @GetMapping("/{id}/copies")
    public List<BookCopyResponse> copies(@PathVariable Long id) {
        return bookService.copies(id).stream().map(ResponseMapper::copy).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public BookResponse create(@Valid @RequestBody BookRequest request) {
        return ResponseMapper.book(bookService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public BookResponse update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return ResponseMapper.book(bookService.update(id, request));
    }
}
