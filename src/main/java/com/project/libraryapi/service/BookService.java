package com.project.libraryapi.service;


import com.project.libraryapi.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BookService {
    Book save(Book book);

    Optional<Book> findById(long id);

    void delete(Book book);

    Book updateBook(Book book);

    Page<Book> findByFilters(Book book, Pageable pageRequest);

    Optional<Book> findByIsbn(String isbn);
}
