package com.project.libraryapi.model.repository;

import com.project.libraryapi.model.entity.Book;
import com.project.libraryapi.model.entity.Loan;
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
}
