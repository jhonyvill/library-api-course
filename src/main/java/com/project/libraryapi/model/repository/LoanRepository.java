package com.project.libraryapi.model.repository;

import com.project.libraryapi.model.entity.Book;
import com.project.libraryapi.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query(value = "SELECT CASE WHEN (COUNT(l.id) > 0) THEN true ELSE false END" +
            " FROM Loan l" +
            " WHERE l.book = :book" +
            " AND (l.returned IS NULL OR l.returned IS false)")
    boolean existsByBookAndNotReturned(@Param("book") Book book);

    @Query(value = "SELECT l" +
            " FROM Loan AS l" +
            " JOIN l.book AS b" +
            " WHERE b.isbn = :isbn" +
            " OR l.customer = :customer")
    Page<Loan> findByBookIsbnOrCustomer(@Param("isbn") String isbn, @Param("customer") String customer, Pageable pageRequest);
}
