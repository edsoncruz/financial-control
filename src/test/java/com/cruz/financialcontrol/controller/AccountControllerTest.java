package com.cruz.financialcontrol.controller;

import com.cruz.financialcontrol.model.dto.account.CreateAccountDTO;
import com.cruz.financialcontrol.model.dto.account.AccountResponseDTO;
import com.cruz.financialcontrol.model.dto.account.UpdateAccountDTO;
import com.cruz.financialcontrol.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private CreateAccountDTO requestDTO;
    private AccountResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new CreateAccountDTO("Savings" );
        responseDTO = new AccountResponseDTO(1L, "Savings", new BigDecimal("1000.00"));
    }

    @Test
    void create_shouldReturnCreatedAccount() {
        when(accountService.create(any(CreateAccountDTO.class))).thenReturn(responseDTO);

        ResponseEntity<AccountResponseDTO> response = accountController.create(requestDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("Savings", response.getBody().name());
        assertEquals(new BigDecimal("1000.00"), response.getBody().balance());
        verify(accountService).create(requestDTO);
    }

    @Test
    void update_shouldReturnUpdatedAccount() {
        UpdateAccountDTO updateRequest = new UpdateAccountDTO("Updated Savings");
        AccountResponseDTO updateResponse = new AccountResponseDTO(1L, "Updated Savings", new BigDecimal("2000.00"));
        when(accountService.update(eq(1L), any(UpdateAccountDTO.class))).thenReturn(updateResponse);

        ResponseEntity<AccountResponseDTO> response = accountController.update(1L, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("Updated Savings", response.getBody().name());
        assertEquals(new BigDecimal("2000.00"), response.getBody().balance());
        verify(accountService).update(1L, updateRequest);
    }

    @Test
    void findById_shouldReturnAccountWithOkStatus() {
        when(accountService.findById(1L)).thenReturn(responseDTO);

        ResponseEntity<AccountResponseDTO> response = accountController.findById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("Savings", response.getBody().name());
        verify(accountService).findById(1L);
    }

    @Test
    void findById_whenNotFound_shouldThrowException() {
        when(accountService.findById(99L))
                .thenThrow(new RuntimeException("Account not found with id: 99"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountController.findById(99L));

        assertEquals("Account not found with id: 99", exception.getMessage());
        verify(accountService).findById(99L);
    }

    @Test
    void deleteById_shouldCallService() {
        doNothing().when(accountService).deleteById(1L);

        ResponseEntity<?> response = accountController.deleteById(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(accountService).deleteById(1L);
    }
}
