package com.project.libraryapi.service;

import com.project.libraryapi.api.dto.BookDTO;
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
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @Test
    @DisplayName("Deve obter um livro por Id")
    public void getBookByIdTest(){
        Book book = createBook();
        book.setId(1L);
        Mockito.when(repository.findById(Mockito.anyLong())).thenReturn(Optional.of(book));

        Optional<Book> foundBook = service.findById(1L);

        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(book.getId());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
    }

    @Test
    @DisplayName("Deve retornar vazio quando não encontrar um livro por Id")
    public void bookNotFoundByIdTest(){
        Mockito.when(repository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        Optional<Book> book = service.findById(1L);

        assertThat(book.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest(){
        Book book = Book.builder().id(1L).build();

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));

        Mockito.verify(repository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Erro ao tentar deletar um livro inexistente")
    public void deleteInexistentBookTest(){
        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        Mockito.verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() {
        Book bookToUpdate = createBook();
        bookToUpdate.setId(1L);
        Book updatedBook = createSavedBook("1234", "Artur", "Viajando o mundo");

        Mockito.when(repository.save(bookToUpdate)).thenReturn(updatedBook);

        Book book = service.updateBook(bookToUpdate);

        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
    }

    @Test
    @DisplayName("Erro ao tentar atualizar um livro inexistente")
    public void updateInexistentBookTest(){
        Book book = new Book();

        Throwable exception = Assertions.catchThrowable(() -> service.updateBook(book));

        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Livro não pode ser nulo.");
        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve filtrar livros pelos parâmetros informados")
    public void findBookByFiltersTest(){
        Book book = createBook();
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> bookList = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(bookList, pageRequest, 1);

        Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Book> pageResult = service.findByFilters(book, pageRequest);

        assertThat(pageResult.getTotalElements()).isEqualTo(1);
        assertThat(pageResult.getContent()).isEqualTo(bookList);
        assertThat(pageResult.getContent().size()).isEqualTo(1);
        assertThat(pageResult.getContent().get(0).getTitle()).isEqualTo(book.getTitle());
        assertThat(pageResult.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(pageResult.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve buscar um livro por ISBN")
    public void findByIsbnTest(){
        Book book = createBook();
        book.setId(1L);

        Mockito.when(repository.findByIsbn(book.getIsbn())).thenReturn(Optional.of(book));

        Optional<Book> foundBookResult = service.findByIsbn("1234");

        assertThat(foundBookResult).isPresent();
        assertThat(foundBookResult.get().getId()).isEqualTo(1L);
        assertThat(foundBookResult.get().getIsbn()).isEqualTo(book.getIsbn());

        Mockito.verify(repository, Mockito.times(1)).findByIsbn(book.getIsbn());
    }

    private Book createBook() {
        return Book.builder()
                .isbn("1234")
                .author("Artur")
                .title("Viajando o mundo")
                .build();
    }

    private Book createSavedBook(String isbn, String author, String title) {
        return Book.builder()
                .id(1L)
                .isbn(isbn)
                .author(author)
                .title(title)
                .build();
    }
}
