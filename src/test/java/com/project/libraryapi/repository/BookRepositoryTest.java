package com.project.libraryapi.repository;

import com.project.libraryapi.model.repository.BookRepository;
import com.project.libraryapi.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir livro na base de dados com ISBN informado")
    public void returnTrueWhenExistISBN(){
        //cenário
        String isbn = "123";
        entityManager.persist(createBook(isbn, "Kamilla", "Viajando o mundo"));

        //execução
        boolean exist = repository.existsByIsbn(isbn);

        //verificação
        assertThat(exist).isTrue();
    }

    @Test
    @DisplayName("Deve retornar falso quando não existir livro na base de dados com ISBN informado")
    public void returnFalseWhenDoesntExistISBN(){
        //cenário
        String isbn = "123";

        //execução
        boolean exist = repository.existsByIsbn(isbn);

        //verificação
        assertThat(exist).isFalse();
    }

    @Test
    @DisplayName("Deve obter um livro por Id")
    public void findByIdTest(){

        Book book = createBook("123", "Kamilla", "Viajando o mundo");
        entityManager.persist(book);

        Optional<Book> foundBook = repository.findById(book.getId());

        assertThat(foundBook.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){
        Book book = createBook("123", "Kamilla", "Viajando o mundo");
        Book savedBook = repository.save(book);

        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest(){
        Book book = createBook("123", "Kamilla", "Viajando o mundo");
        Book savedBook = entityManager.persist(book);

        repository.delete(savedBook);
        Book foundBook = entityManager.find(Book.class, savedBook.getId());

        assertThat(foundBook).isNull();
    }

    @Test
    @DisplayName("Deve filtrar livros pelos parâmetros informados")
    public void findBookByFiltersTest(){
        Book book = Book.builder().author("Kamilla").title("Viajando o mundo").build();
        entityManager.persist(book);
        PageRequest pageRequest = PageRequest.of(0, 10);
        Example<Book> exampleBook = Example.of(book,
                ExampleMatcher.matching()
                        .withIgnoreCase()
                        .withIgnoreNullValues()
                        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));

        Page<Book> bookListResult = repository.findAll(exampleBook, pageRequest);

        assertThat(bookListResult).isNotNull();
        assertThat(bookListResult.getTotalElements()).isEqualTo(1);
        assertThat(bookListResult.getContent().size()).isEqualTo(1);
        assertThat(bookListResult.getContent().get(0).getTitle()).isEqualTo(book.getTitle());
        assertThat(bookListResult.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(bookListResult.getPageable().getPageSize()).isEqualTo(10);
    }

    private Book createBook(String isbn, String author, String title) {
        return Book.builder().isbn(isbn).author(author).title(title).build();
    }

}
