package com.project.libraryapi.service;


import com.project.libraryapi.model.entity.Book;

import java.util.Optional;

public interface BookService {
    Book save(Book book);

    Optional<Book> findById(long id);
}
