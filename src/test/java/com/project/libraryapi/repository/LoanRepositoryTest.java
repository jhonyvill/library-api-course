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

        Book book = createBook();
        entityManager.persist(book);

        Loan loan = Loan.builder().customer("Jhony").book(book).loanDate(LocalDate.now()).build();
        entityManager.persist(loan);

        boolean result = repository.existsByBookAndNotReturned(book);

        assertThat(result).isTrue();
    }
}
