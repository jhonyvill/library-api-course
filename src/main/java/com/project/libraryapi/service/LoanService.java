package com.project.libraryapi.service;

import com.project.libraryapi.api.dto.LoanFilterDTO;
import com.project.libraryapi.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LoanService {


    Loan save(Loan loan);

    Optional<Loan> findById(long id);

    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDTO loanFilterDTO, Pageable pageRequest);
}
