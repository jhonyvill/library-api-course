package com.project.libraryapi.repository;

import com.project.libraryapi.model.entity.Book;
import com.project.libraryapi.model.entity.Loan;
import com.project.libraryapi.model.repository.LoanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static com.project.libraryapi.service.BookServiceTest.createBook;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    LoanRepository repository;

    @Test
    @DisplayName("Deve verificar se existe empréstimo não devolvido para o livro")
    public void existsByBookAndNotReturnedTest(){

        Loan loan = createAndPersistLoan();

        boolean result = repository.existsByBookAndNotReturned(loan.getBook());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve buscar empréstimo pelo isbn do livro ou costumer")
    public void findByBookIsbnOrCustomerTest(){
        Loan loan = createAndPersistLoan();

        Page<Loan> pageResult = repository.findByBookIsbnOrCustomer(
                "1234", "Jhony", PageRequest.of(0, 10));

        assertThat(pageResult.getContent()).hasSize(1);
        assertThat(pageResult.getTotalElements()).isEqualTo(1);
        assertThat(pageResult.getPageable().getPageSize()).isEqualTo(10);
        assertThat(pageResult.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(pageResult.getContent().get(0).getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(pageResult.getContent().get(0).getBook().getIsbn()).isEqualTo(loan.getBook().getIsbn());
    }

    private Loan createAndPersistLoan() {
        Book book = createBook();
        entityManager.persist(book);

        Loan loan = Loan.builder().customer("Jhony").book(book).loanDate(LocalDate.now()).build();
        entityManager.persist(loan);
        return loan;
    }
}
