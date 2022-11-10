package com.project.libraryapi.service.impl;

import com.project.libraryapi.api.dto.LoanInputDTO;
import com.project.libraryapi.exception.BusinessException;
import com.project.libraryapi.model.entity.Loan;
import com.project.libraryapi.model.repository.LoanRepository;
import com.project.libraryapi.service.LoanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    @Override
    public Optional<Loan> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public Loan update(Loan loan) {
        return repository.save(loan);
    }

    @Override
    public Page<Loan> find(LoanInputDTO loanInputDTO, Pageable pageRequest) {
        return repository.findByBookIsbnOrCustomer(loanInputDTO.getIsbn(), loanInputDTO.getCustomer(), pageRequest);
    }
}
