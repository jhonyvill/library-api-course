package com.project.libraryapi.service;

import com.project.libraryapi.api.dto.LoanInputDTO;
import com.project.libraryapi.exception.BusinessException;
import com.project.libraryapi.model.entity.Book;
import com.project.libraryapi.model.entity.Loan;
import com.project.libraryapi.model.repository.LoanRepository;
import com.project.libraryapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
        Loan loan = createLoan();
        Loan savedLoan = createLoan();
        savedLoan.setReturned(false);

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
        Loan loan = createLoan();

        when(repository.existsByBookAndNotReturned(loan.getBook())).thenReturn(true);

        Throwable exception = catchThrowable(() -> loanService.save(loan));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already borrowed.");

        verify(repository, never()).save(loan);
    }

    @Test
    @DisplayName("Deve obter as informações de um empréstimo pelo ID")
    public void findLoanByIdTest(){
        long id = 1L;
        Loan loan = createLoan();
        loan.setId(id);

        when(repository.findById(loan.getId())).thenReturn(Optional.of(loan));

        Optional<Loan> result = loanService.findById(id);

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(loan.getId());
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(repository).findById(loan.getId());
    }

    @Test
    @DisplayName("Deve atualizar um empréstimo")
    public void updateLoanTest(){
        Loan loan = createLoan();
        loan.setId(1L);
        loan.setReturned(true);

        when(repository.save(Mockito.any(Loan.class))).thenReturn(loan);

        Loan updatedLoan = loanService.update(loan);

        assertThat(updatedLoan.getReturned()).isTrue();
        verify(repository).save(loan);
    }

    @Test
    @DisplayName("Deve filtrar empréstimos pelas propriedades")
    public void findLoanTest(){
        //cenário
        Loan loan = createLoan();
        loan.setId(1L);
        loan.getBook().setIsbn("123");

        List<Loan> loans = List.of(loan);
        PageRequest pageRequest = PageRequest.of(0,10);
        LoanInputDTO loanInputDTO = LoanInputDTO.builder().isbn("123").customer("Jhony").build();
        Page<Loan> page = new PageImpl<Loan>(loans, pageRequest, loans.size());

        Mockito.when(repository.findByBookIsbnOrCustomer(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(Pageable.class))).thenReturn(page);

        //execução
        Page<Loan> pageResult = loanService.find(loanInputDTO, pageRequest);

        //validações
        assertThat(pageResult.getTotalElements()).isEqualTo(1);
        assertThat(pageResult.getContent()).isEqualTo(loans);
        assertThat(pageResult.getContent().size()).isEqualTo(1);
        assertThat(pageResult.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(pageResult.getPageable().getPageSize()).isEqualTo(10);
    }

    public static Loan createLoan() {
        return Loan.builder()
                .customer("Jhony")
                .book(Book.builder().id(1L).build())
                .loanDate(LocalDate.now())
                .build();
    }
}
