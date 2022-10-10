package com.project.libraryapi.service.impl;

import com.project.libraryapi.exception.BusinessException;
import com.project.libraryapi.model.BookRepository;
import com.project.libraryapi.model.entity.Book;
import com.project.libraryapi.service.BookService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        if(repository.existsByIsbn(book.getIsbn()))
            throw new BusinessException("ISBN já cadastrado");
        return repository.save(book);
    }

    @Override
    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Book book) {
        if (book == null || book.getId() == null)
            throw new IllegalArgumentException("Livro não pode ser nulo.");
        repository.delete(book);
    }

    @Override
    public Book updateBook(Book book) {
        if (book == null || book.getId() == null)
            throw new IllegalArgumentException("Livro não pode ser nulo.");
       return repository.save(book);
    }

    @Override
    public Page<Book> findByFilters(Book book, Pageable pageRequest) {
        Example<Book> exampleBook = Example.of(book,
                ExampleMatcher.matching()
                        .withIgnoreCase()
                        .withIgnoreNullValues()
                        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        return repository.findAll(exampleBook, pageRequest);
    }
}
