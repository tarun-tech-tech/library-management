package com.smartlib.service;

import com.smartlib.api.dto.BookRequest;
import com.smartlib.domain.Book;
import com.smartlib.domain.BookCopy;
import com.smartlib.domain.CopyStatus;
import com.smartlib.repository.BookCopyRepository;
import com.smartlib.repository.BookRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;

    public BookService(BookRepository bookRepository, BookCopyRepository bookCopyRepository) {
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
    }

    @Transactional(readOnly = true)
    public List<Book> find(String query) {
        if (query == null || query.isBlank()) return bookRepository.findAllByOrderByTitleAsc();
        String normalized = query.trim();
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                normalized, normalized, normalized, normalized);
    }

    @Transactional(readOnly = true)
    public Book get(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book " + id + " was not found"));
    }

    @Transactional
    public Book create(BookRequest request) {
        Book book = new Book(request.title().trim(), request.author().trim(), request.isbn().trim(),
                request.category().trim(), trimToNull(request.publisher()), request.publicationYear(), request.copies());
        try {
            bookRepository.saveAndFlush(book);
            createCopies(book, request.copies());
            return book;
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("A book with that ISBN already exists");
        }
    }

    @Transactional
    public Book update(Long id, BookRequest request) {
        Book book = get(id);
        int existingCopies = book.getTotalCopies();
        if (request.copies() < existingCopies) {
            throw new ConflictException("Copies cannot be reduced from the catalog; mark individual copies as unavailable instead");
        }
        book.setTitle(request.title().trim());
        book.setAuthor(request.author().trim());
        book.setIsbn(request.isbn().trim());
        book.setCategory(request.category().trim());
        book.setPublisher(trimToNull(request.publisher()));
        book.setPublicationYear(request.publicationYear());
        int additionalCopies = request.copies() - existingCopies;
        if (additionalCopies > 0) {
            book.addCopies(additionalCopies);
            createCopies(book, additionalCopies);
        }
        try {
            return bookRepository.save(book);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("A book with that ISBN already exists");
        }
    }

    @Transactional(readOnly = true)
    public List<BookCopy> copies(Long bookId) {
        get(bookId);
        return bookCopyRepository.findByBookIdOrderByInventoryCodeAsc(bookId);
    }

    private void createCopies(Book book, int quantity) {
        for (int index = 0; index < quantity; index++) {
            String inventoryCode = "SL-" + UUID.randomUUID();
            bookCopyRepository.save(new BookCopy(book, inventoryCode, LocalDate.now()));
        }
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
