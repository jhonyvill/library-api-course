package com.project.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.libraryapi.api.dto.LoanDTO;
import com.project.libraryapi.api.dto.ReturnedLoanDTO;
import com.project.libraryapi.exception.BusinessException;
import com.project.libraryapi.model.entity.Book;
import com.project.libraryapi.model.entity.Loan;
import com.project.libraryapi.service.BookService;
import com.project.libraryapi.service.LoanService;

import org.hamcrest.Matchers;
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

import java.time.LocalDate;
import java.util.Optional;

import static com.project.libraryapi.api.resource.BookControllerTest.createBook;
import static com.project.libraryapi.service.LoanServiceTest.createLoan;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

    private static final String LOAN_API = "/api/loans";
    @Autowired
    MockMvc mvc;

    @MockBean
    BookService bookService;

    @MockBean
    LoanService loanService;

    @Test
    @DisplayName("Deve criar um empréstimo")
    public void createLoanTest() throws Exception {
        String isbn = "123";
        LoanDTO loanDTO = createLoanDTO(isbn, "Jhony");

        Book book = createBook(1L,"Jhony", "As Aventuras", isbn);
        Loan loan = createLoan();
        loan.setId(1L);
        loan.setBook(book);

        BDDMockito.given(bookService.findByIsbn(isbn)).willReturn(Optional.of(book));
        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        String json = new ObjectMapper().writeValueAsString(loanDTO);
        MockHttpServletRequestBuilder request = createLoanPostRequest(json);

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer empréstimo de livro inexistente")
    public void createLoanInvalidIsbnTest() throws Exception {
        String isbn = "123";
        LoanDTO loanDTO = createLoanDTO(isbn, "Fulano");

        BDDMockito.given(bookService.findByIsbn(isbn)).willReturn(Optional.empty());

        String json = new ObjectMapper().writeValueAsString(loanDTO);
        MockHttpServletRequestBuilder request = createLoanPostRequest(json);

        mvc.perform(request)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("errors", Matchers.hasSize(1)))
            .andExpect(jsonPath("errors[0]").value("Book not found for passed isbn"));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer empréstimo de livro já emprestado")
    public void borrowedBookLoanTest() throws Exception {
        String isbn = "123";
        LoanDTO loanDTO = createLoanDTO(isbn, "Fulano");
        Book book = createBook(1L,"Fulano", "As Aventuras", isbn);

        BDDMockito.given(bookService.findByIsbn(isbn)).willReturn(Optional.of(book));
        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willThrow(new BusinessException("Book already borrowed"));

        String json = new ObjectMapper().writeValueAsString(loanDTO);
        MockHttpServletRequestBuilder request = createLoanPostRequest(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book already borrowed"));
    }

    @Test
    @DisplayName("Deve realizar a devolução de livro emprestado")
    public void returnBookTest() throws Exception {
        ReturnedLoanDTO returnedLoanDTO = ReturnedLoanDTO.builder().returned(true).build();
        Loan loan = Loan.builder().id(1L).build();

        BDDMockito.given(loanService.findById(Mockito.anyLong())).willReturn(Optional.of(loan));
        String json = new ObjectMapper().writeValueAsString(returnedLoanDTO);

        mvc.perform(MockMvcRequestBuilders
                        .patch(LOAN_API.concat("/1"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
        Mockito.verify(loanService, Mockito.times(1)).update(loan);
    }

    @Test
    @DisplayName("Deve retornar 'Not Found' quando tentar devolver livro de empréstimo inexistente")
    public void returnInexistentBookTest() throws Exception {
        ReturnedLoanDTO returnedLoanDTO = ReturnedLoanDTO.builder().returned(true).build();

        BDDMockito.given(loanService.findById(Mockito.anyLong())).willReturn(Optional.empty());
        String json = new ObjectMapper().writeValueAsString(returnedLoanDTO);

        mvc.perform(MockMvcRequestBuilders
                        .patch(LOAN_API.concat("/1"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }


    private MockHttpServletRequestBuilder createLoanPostRequest(String json) {
        return MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
    }

    private LoanDTO createLoanDTO(String isbn, String customer) {
        return LoanDTO.builder().isbn(isbn).customer(customer).build();
    }
}
