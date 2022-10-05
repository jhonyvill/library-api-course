package com.project.libraryapi.service;

import com.project.libraryapi.exception.BusinessException;
import com.project.libraryapi.model.BookRepository;
import com.project.libraryapi.model.entity.Book;
import com.project.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;
    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){
        //cenário
        Book book = createBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        Mockito.when(repository.save(book)).thenReturn(createSavedBook("1234", "Artur", "Viajando o mundo"));

        //execução
        Book savedBook = service.save(book);

        //verificação
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo(book.getIsbn());
        assertThat(savedBook.getAuthor()).isEqualTo(book.getAuthor());
        assertThat(savedBook.getTitle()).isEqualTo(book.getTitle());

    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar livro com ISBN já existente")
    public void shouldNotSavedBookWithDuplicatedISBN(){
        Book book = createBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("ISBN já cadastrado");

        Mockito.verify(repository, Mockito.never()).save(book);
    }

    private Book createBook() {
        return Book.builder().isbn("1234").author("Artur").title("Viajando o mundo").build();
    }

    private Book createSavedBook(String isbn, String author, String title) {
        return Book.builder().id(1L).isbn(isbn).author(author).title(title).build();
    }
}
