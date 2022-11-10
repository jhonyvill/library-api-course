package com.project.libraryapi.service.impl;

import com.project.libraryapi.exception.BusinessException;
import com.project.libraryapi.model.repository.LoanRepository;
import com.project.libraryapi.model.entity.Loan;
import com.project.libraryapi.service.LoanService;
import org.springframework.stereotype.Service;

@Service
public class LoanServiceImpl implements LoanService {

    LoanRepository repository;

    public LoanServiceImpl(LoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Loan save(Loan loan) {
        if (repository.existsByBookAndNotReturned(loan.getBook()))
            throw new BusinessException("Book already borrowed.");
        return repository.save(loan);
    }
}
