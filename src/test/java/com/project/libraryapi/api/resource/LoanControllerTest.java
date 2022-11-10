package com.project.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.libraryapi.api.dto.LoanDTO;
import com.project.libraryapi.api.dto.LoanInputDTO;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static com.project.libraryapi.api.resource.BookControllerTest.createBook;
import static com.project.libraryapi.service.LoanServiceTest.createLoan;
import static org.hamcrest.Matchers.hasSize;
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
        LoanInputDTO loanDTO = createLoanDTO("123", "Jhony");

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
        LoanInputDTO loanDTO = createLoanDTO("123", "Jhony");

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
        LoanInputDTO loanDTO = createLoanDTO("123", "Jhony");
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

    private MockHttpServletRequestBuilder createLoanPostRequest(String json) {
        return MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
    }

    private LoanInputDTO createLoanDTO(String isbn, String customer) {
        return LoanInputDTO.builder().isbn(isbn).customer(customer).build();
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

    @Test
    @DisplayName("Deve filtrar empréstimos")
    public void findLoantest() throws Exception{
        Long id = 1L;
        Book book = createBook(id,"Artur", "As Aventuras", "001");
        Loan loan = createLoan();
        loan.setId(id);
        loan.setBook(book);

        BDDMockito.given(loanService.find(Mockito.any(LoanInputDTO.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0, 10), 1));

        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=10",
                book.getIsbn(), loan.getCustomer());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(10))
                .andExpect(jsonPath("pageable.pageNumber").value(0));

    }

}
