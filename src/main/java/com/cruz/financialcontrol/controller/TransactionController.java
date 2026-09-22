package com.cruz.financialcontrol.controller;

import com.cruz.financialcontrol.model.dto.transaction.CreateTransactionDTO;
import com.cruz.financialcontrol.model.dto.transaction.TransactionResponseDTO;
import com.cruz.financialcontrol.model.dto.transaction.UpdateTransactionDTO;
import com.cruz.financialcontrol.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Class Name: TransactionController
 * Description:
 *
 * @author edson
 * @date 09/09/2026
 */
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> create(@Valid @RequestBody CreateTransactionDTO createTransactionDTO) {
        TransactionResponseDTO transactionResponseDTO = transactionService.create(createTransactionDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(transactionResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateTransactionDTO updateTransactionDTO) {
        TransactionResponseDTO transactionResponseDTO = transactionService.update(id, updateTransactionDTO);

        return ResponseEntity.status(HttpStatus.OK).body(transactionResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        transactionService.deleteById(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> findById(@PathVariable Long id) {
        TransactionResponseDTO transactionResponseDTO = transactionService.findById(id);

        return ResponseEntity.status(HttpStatus.OK).body(transactionResponseDTO);
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponseDTO>> findAll(@PageableDefault(size = 20, sort = "date") Pageable pageable) {
        Page<TransactionResponseDTO> allTransactions = transactionService.findAll(pageable);

        return ResponseEntity.status(HttpStatus.OK).body(allTransactions);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<TransactionResponseDTO> confirm(@PathVariable Long id) {
        TransactionResponseDTO transactionResponseDTO = transactionService.confirm(id);

        return ResponseEntity.status(HttpStatus.OK).body(transactionResponseDTO);
    }
}
