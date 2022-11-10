package com.project.libraryapi.api.resource;

import com.project.libraryapi.api.dto.BookDTO;
import com.project.libraryapi.model.entity.Book;
import com.project.libraryapi.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final ModelMapper modelMapper;

    public BookController(BookService bookService, ModelMapper mapper) {
        this.bookService = bookService;
        this.modelMapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@RequestBody @Valid BookDTO bookDTO){
        Book entity = modelMapper.map(bookDTO, Book.class);
        entity = bookService.save(entity);
        return toBookDTO(entity);
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDTO getBookById(@PathVariable Long id){
        return bookService.findById(id)
                .map(book -> toBookDTO(book))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        Book book = bookService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        bookService.delete(book);
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDTO update(@PathVariable Long id, BookDTO bookDTO){
        return bookService.findById(id)
                .map(book ->{   book.setAuthor(bookDTO.getAuthor());
                                book.setTitle(bookDTO.getTitle());
                                book = bookService.updateBook(book);
                                return toBookDTO(book);
                }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public Page<BookDTO> findByFilters(BookDTO bookDTO, Pageable pageRequest){
        Book filter = toBookEntity(bookDTO);
        Page<Book> result = bookService.findByFilters(filter, pageRequest);
        List<BookDTO> resultList = result.getContent()
                .stream()
                .map(entity -> toBookDTO(entity))
                .collect(Collectors.toList());

        return new PageImpl<BookDTO>(resultList, pageRequest, result.getTotalElements());
    }

    private BookDTO toBookDTO(Book book) {
        return modelMapper.map(book, BookDTO.class);
    }

    private Book toBookEntity(BookDTO bookDTO) {
        return modelMapper.map(bookDTO, Book.class);
    }

}
