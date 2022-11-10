package com.project.libraryapi.api.resource;

import com.project.libraryapi.api.dto.BookDTO;
import com.project.libraryapi.api.dto.LoanDTO;
import com.project.libraryapi.api.dto.LoanFilterDTO;
import com.project.libraryapi.api.dto.ReturnedLoanDTO;
import com.project.libraryapi.model.entity.Book;
import com.project.libraryapi.model.entity.Loan;
import com.project.libraryapi.service.BookService;
import com.project.libraryapi.service.LoanService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final BookService bookService;
    private final LoanService loanService;
    private final ModelMapper modelMapper;

    public LoanController(BookService bookService, LoanService loanService, ModelMapper modelMapper) {
        this.bookService = bookService;
        this.loanService = loanService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createLoan(@RequestBody LoanDTO loanDTO){
        Book book = bookService.findByIsbn(loanDTO.getIsbn())
                                                    .orElseThrow(() -> new ResponseStatusException (HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));
        Loan entity = Loan.builder()
                            .book(book)
                            .customer(loanDTO.getCustomer())
                            .loanDate(LocalDate.now())
                            .build();

        entity = loanService.save(entity);
        return entity.getId();
    }

    @PatchMapping("{id}")
    public Loan updateLoan(@PathVariable Long id, @RequestBody ReturnedLoanDTO dto){
        Loan foundLoan = loanService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        foundLoan.setReturned(dto.getReturned());
        return loanService.update(foundLoan);
    }

    @GetMapping
    public Page<LoanDTO> find(LoanFilterDTO filterDTO, Pageable pageRequest){
        Page<Loan> result = loanService.find(filterDTO, pageRequest);
        List<LoanDTO> loans = result.getContent()
                                    .stream()
                                    .map(loan -> toLoanDTO(loan))
                                    .collect(Collectors.toList());
        return new PageImpl<LoanDTO>(loans, pageRequest, result.getTotalElements());
    }

    private LoanDTO toLoanDTO(Loan loan) {
        return modelMapper.map(loan, LoanDTO.class);
    }
}
