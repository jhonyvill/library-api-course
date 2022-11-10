package com.project.libraryapi.service;

import com.project.libraryapi.exception.BusinessException;
import com.project.libraryapi.model.entity.Book;
import com.project.libraryapi.model.entity.Loan;
import com.project.libraryapi.model.repository.LoanRepository;
import com.project.libraryapi.service.impl.BookServiceImpl;
import com.project.libraryapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    LoanService loanService;

    @MockBean
    LoanRepository repository;

    @BeforeEach
    public void setUp(){
        this.loanService = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um empréstimo")
    public void saveLoanTest(){
        String customer = "Jhony";
        Loan loan = createLoan(customer);

        Loan savedLoan = Loan.builder()
                .id(1L)
                .customer(customer)
                .book(Book.builder().id(1L).build())
                .loanDate(LocalDate.now())
                .returned(false)
                .build();

        when(repository.existsByBookAndNotReturned(loan.getBook())).thenReturn(false);
        when(repository.save(loan)).thenReturn(savedLoan);

        Loan returnedLoan = loanService.save(loan);

        assertThat(returnedLoan.getId()).isEqualTo(savedLoan.getId());
        assertThat(returnedLoan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(returnedLoan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        assertThat(returnedLoan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao salvar um empréstimo com livro já emprestado")
    public void borrowedBookSaveTest(){
        Loan loan = createLoan("Jhony");

        when(repository.existsByBookAndNotReturned(loan.getBook())).thenReturn(true);

        Throwable exception = catchThrowable(() -> loanService.save(loan));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already borrowed.");

        verify(repository, never()).save(loan);
    }

    private Loan createLoan(String customer) {
        return Loan.builder()
                .customer(customer)
                .book(Book.builder().id(1L).build())
                .loanDate(LocalDate.now())
                .build();
    }
}
