package com.cruz.financialcontrol.controller;

import com.cruz.financialcontrol.model.dto.account.AccountResponseDTO;
import com.cruz.financialcontrol.model.dto.account.CreateAccountDTO;
import com.cruz.financialcontrol.model.dto.account.UpdateAccountDTO;
import com.cruz.financialcontrol.service.AccountService;
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
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponseDTO> create(@Valid @RequestBody CreateAccountDTO createAccountDTO){
        AccountResponseDTO accountResponseDTO = this.accountService.create(createAccountDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateAccountDTO updateAccountDTO){
        AccountResponseDTO accountResponseDTO = this.accountService.update(id, updateAccountDTO);

        return ResponseEntity.status(HttpStatus.OK).body(accountResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        this.accountService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> findById(@PathVariable Long id) {
        AccountResponseDTO accountResponseDTO = this.accountService.findById(id);

        return ResponseEntity.status(HttpStatus.OK).body(accountResponseDTO);
    }

    @GetMapping
    public ResponseEntity<Page<AccountResponseDTO>> findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<AccountResponseDTO> accountResponseDTOs = this.accountService.findAll(pageable);

        return ResponseEntity.status(HttpStatus.OK).body(accountResponseDTOs);
    }
}

