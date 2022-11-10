package com.project.libraryapi.service;

import com.project.libraryapi.model.entity.Loan;

import java.util.Optional;

public interface LoanService {


    Loan save(Loan loan);

    Optional<Loan> findById(long id);

    Loan update(Loan loan);
}
