package com.smartlib.service;

import com.smartlib.api.dto.BookCopyStatusRequest;
import com.smartlib.domain.BookCopy;
import com.smartlib.domain.CopyStatus;
import com.smartlib.repository.BookCopyRepository;
import com.smartlib.repository.BookRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;

    public BookCopyService(BookCopyRepository bookCopyRepository, BookRepository bookRepository, BookService bookService) {
        this.bookCopyRepository = bookCopyRepository;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
    }

    @Transactional(readOnly = true)
    public List<BookCopy> find(Long bookId) {
        return bookId == null ? bookCopyRepository.findAllByOrderByInventoryCodeAsc()
                : bookService.copies(bookId);
    }

    @Transactional(readOnly = true)
    public BookCopy get(Long id) {
        return bookCopyRepository.findWithBookById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book copy " + id + " was not found"));
    }

    @Transactional
    public BookCopy changeStatus(Long id, BookCopyStatusRequest request) {
        BookCopy copy = bookCopyRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book copy " + id + " was not found"));
        CopyStatus oldStatus = copy.getStatus();
        CopyStatus newStatus = request.status();
        if (oldStatus == CopyStatus.ISSUED || newStatus == CopyStatus.ISSUED) {
            throw new ConflictException("Loan status is managed only by the loan issue and return operations");
        }
        var book = bookRepository.findByIdForUpdate(copy.getBook().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Book was not found for copy " + id));
        if (oldStatus != CopyStatus.AVAILABLE && newStatus == CopyStatus.AVAILABLE) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
        } else if (oldStatus == CopyStatus.AVAILABLE && newStatus != CopyStatus.AVAILABLE) {
            book.setAvailableCopies(book.getAvailableCopies() - 1);
        }
        copy.setStatus(newStatus);
        return copy;
    }
}
