package com.project.libraryapi.api.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.libraryapi.api.dto.BookDTO;
import com.project.libraryapi.exception.BusinessException;
import com.project.libraryapi.model.entity.Book;
import com.project.libraryapi.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService bookService;


    @Test
    @DisplayName("Deve criar um livro com sucesso.")
    public void createBookTest() throws Exception {

        BookDTO bookDTO = createBookDTO("Artur", "As Aventuras", "001");
        Book savedBook = createBook(10L, "Artur", "As Aventuras", "001");

        BDDMockito.given(bookService.save(Mockito.any(Book.class))).willReturn(savedBook);
        String json = new ObjectMapper().writeValueAsString(bookDTO);

        MockHttpServletRequestBuilder request = createPostRequest(json);

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(10L))
                .andExpect(jsonPath("title").value(bookDTO.getTitle()))
                .andExpect(jsonPath("author").value(bookDTO.getAuthor()))
                .andExpect(jsonPath("isbn").value(bookDTO.getIsbn()));

    }

    @Test
    @DisplayName("Deve lançar erro de validação quando não houver dados suficientes para criação de livro.")
    public void createInvalidBookTest() throws Exception {
        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = createPostRequest(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));
    }

    @Test
    @DisplayName("Deve lançar erro quando já houver livro com mesmo ISBN")
    public void createBookWithDuplicatedIsbnTest() throws Exception {
        BookDTO bookDTO = createBookDTO("Artur", "As Aventuras", "001");
        String messageError = "ISBN já cadastrado.";

        String json = new ObjectMapper().writeValueAsString(bookDTO);
        BDDMockito.given(bookService.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(messageError));

        MockHttpServletRequestBuilder request = createPostRequest(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(messageError));
    }
    @Test
    @DisplayName("Deve obter informações de um livro")
    public void getBookByIdTest() throws Exception {
        Long id = 10L;
        Book book =  createBook(10L, "Artur", "As Aventuras", "001");
        BDDMockito.given(bookService.findById(id)).willReturn(Optional.of(book));

        MockHttpServletRequestBuilder request = createGetRequest(id);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(book.getTitle()))
                .andExpect(jsonPath("author").value(book.getAuthor()))
                .andExpect(jsonPath("isbn").value(book.getIsbn()));
    }

    @Test
    @DisplayName("Deve retornar 'Not Found' quando não encontrar livro com o id informado")
    public void bookNotFoundTest() throws Exception {
        Long id = 10L;
        BDDMockito.given(bookService.findById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = createGetRequest(id);

        mvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um livro por id")
    public void deleteBookTest() throws Exception {
        Long id = 1L;
        BDDMockito.given(bookService.findById(Mockito.anyLong()))
                .willReturn(Optional.of(Book.builder().id(id).build()));

        MockHttpServletRequestBuilder request = createDeleteRequest(id);

        mvc.perform(request).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 'Not Found' quando não encontrar livro para deletar")
    public void deleteInexistentBookTest() throws Exception {
        BDDMockito.given(bookService.findById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = createDeleteRequest(1L);

        mvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() throws Exception {
        Long id = 1L;
        BookDTO bookDTO = BookDTO.builder().author("Fulano").title("Viajando o mundo").build();
        String json = new ObjectMapper().writeValueAsString(bookDTO);

        Book bookToUpdate = createBook(1L, "Artur", "As Aventuras", "001");
        BDDMockito.given(bookService.findById(id)).willReturn(Optional.of(bookToUpdate));

        Book bookUpdated = createBook(1L, "Fulano", "Viajando o mundo", "001");
        BDDMockito.given(bookService.updateBook(bookToUpdate)).willReturn(bookUpdated);

        MockHttpServletRequestBuilder request = createPutRequest(id, json);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(bookUpdated.getId()))
                .andExpect(jsonPath("author").value(bookUpdated.getAuthor()))
                .andExpect(jsonPath("title").value(bookUpdated.getTitle()))
                .andExpect(jsonPath("isbn").value(bookUpdated.getIsbn()));
    }
    @Test
    @DisplayName("Deve retornar 'Not Found' quando não encontrar o livro para atualizar")
    public void updateInexistentBookTest() throws Exception{
        Long id = 1L;
        BookDTO bookDTO = BookDTO.builder().author("Fulano").title("Viajando o mundo").build();
        String json = new ObjectMapper().writeValueAsString(bookDTO);
        BDDMockito.given(bookService.findById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = createPutRequest(id, json);

        mvc.perform(request).andExpect(status().isNotFound());
        Mockito.verify(bookService, Mockito.never()).updateBook(Mockito.any());
    }

    private MockHttpServletRequestBuilder createPutRequest(Long id, String json) {
        return MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
    }

    private MockHttpServletRequestBuilder createDeleteRequest(Long id) {
        return MockMvcRequestBuilders.delete(BOOK_API.concat("/" + id));
    }

    private MockHttpServletRequestBuilder createGetRequest(Long id) {
        return MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id)).accept(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder createPostRequest(String json) {
        return MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);
    }


    private BookDTO createBookDTO(String author, String title, String isbn) {
        return BookDTO.builder().author(author).title(title).isbn(isbn).build();
    }

    private Book createBook(Long id, String author, String title, String isbn) {
        return Book.builder().id(id).author(author).title(title).isbn(isbn).build();
    }
}
