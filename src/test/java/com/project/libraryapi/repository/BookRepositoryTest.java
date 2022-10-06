package com.project.libraryapi.repository;

import com.project.libraryapi.model.BookRepository;
import com.project.libraryapi.model.entity.Book;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
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
        entityManager.persist(createBook(isbn, "Fulano", "Viajando o mundo"));

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

        Book book = createBook("123", "Fulano", "Viajando o mundo");
        entityManager.persist(book);

        Optional<Book> foundBook = repository.findById(book.getId());

        assertThat(foundBook.isPresent()).isTrue();
    }

    private Book createBook(String isbn, String author, String title) {
        return Book.builder().isbn(isbn).author(author).title(title).build();
    }

}
