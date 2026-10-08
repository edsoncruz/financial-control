package com.cruz.financialcontrol.controller;

import com.cruz.financialcontrol.model.dto.transactioncategory.CreateTransactionCategoryDTO;
import com.cruz.financialcontrol.model.dto.transactioncategory.TransactionCategoryResponseDTO;
import com.cruz.financialcontrol.model.dto.transactioncategory.UpdateTransactionCategoryDTO;
import com.cruz.financialcontrol.service.TransactionCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transaction-categories")
public class TransactionCategoryController {

    private final TransactionCategoryService transactionCategoryService;

    @PostMapping
    public ResponseEntity<TransactionCategoryResponseDTO> create(
            @Valid @RequestBody CreateTransactionCategoryDTO createDTO) {
        TransactionCategoryResponseDTO response = transactionCategoryService.create(createDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionCategoryResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionCategoryDTO updateDTO) {
        TransactionCategoryResponseDTO response = transactionCategoryService.update(id, updateDTO);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        transactionCategoryService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionCategoryResponseDTO> findById(@PathVariable Long id) {
        TransactionCategoryResponseDTO response = transactionCategoryService.findById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<TransactionCategoryResponseDTO>> findAll(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<TransactionCategoryResponseDTO> responses = transactionCategoryService.findAll(pageable);

        return ResponseEntity.ok(responses);
    }
}
