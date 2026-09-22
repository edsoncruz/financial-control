package com.cruz.financialcontrol.service;

import com.cruz.financialcontrol.exception.BusinessRuleException;
import com.cruz.financialcontrol.exception.NotFoundException;
import com.cruz.financialcontrol.model.dto.transaction.CreateTransactionDTO;
import com.cruz.financialcontrol.model.dto.transaction.TransactionResponseDTO;
import com.cruz.financialcontrol.model.dto.transaction.UpdateTransactionDTO;
import com.cruz.financialcontrol.model.entity.Account;
import com.cruz.financialcontrol.model.entity.Transaction;
import com.cruz.financialcontrol.model.entity.User;
import com.cruz.financialcontrol.model.enums.TransactionStatus;
import com.cruz.financialcontrol.model.enums.TransactionType;
import com.cruz.financialcontrol.model.mapper.TransactionMapper;
import com.cruz.financialcontrol.repository.TransactionRepository;
import com.cruz.financialcontrol.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private SecurityUtils securityUtils;

    private TransactionService transactionService;

    private static final Long CURRENT_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, accountService, transactionMapper, securityUtils);
    }

    private Account accountOwnedByCurrentUser(Long accountId) {
        User user = new User();
        user.setId(CURRENT_USER_ID);

        Account account = new Account();
        account.setId(accountId);
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        return account;
    }

    @Test
    void create_shouldIncreaseAccountBalance_whenIncomeAndConfirmed() {
        CreateTransactionDTO dto = new CreateTransactionDTO(
                "Salary", new BigDecimal("500.00"), LocalDate.now(), TransactionType.INCOME, TransactionStatus.CONFIRMED, 10L);

        Account account = accountOwnedByCurrentUser(10L);
        Transaction mapped = new Transaction();
        mapped.setType(TransactionType.INCOME);
        mapped.setStatus(TransactionStatus.CONFIRMED);
        mapped.setAmount(new BigDecimal("500.00"));

        when(accountService.findOwnedAccountOrThrow(10L)).thenReturn(account);
        when(transactionMapper.toEntity(dto)).thenReturn(mapped);
        when(transactionRepository.saveAndFlush(mapped)).thenReturn(mapped);
        when(transactionMapper.toResponseDTO(mapped)).thenReturn(
                new TransactionResponseDTO(1L, "Salary", new BigDecimal("500.00"), LocalDate.now(), TransactionType.INCOME, TransactionStatus.CONFIRMED, 10L));

        transactionService.create(dto);

        verify(accountService).addBalance(10L, new BigDecimal("500.00"));
        verify(accountService, never()).subtractBalance(any(), any());
    }

    @Test
    void create_shouldDecreaseAccountBalance_whenExpenseAndConfirmed() {
        CreateTransactionDTO dto = new CreateTransactionDTO(
                "Groceries", new BigDecimal("80.00"), LocalDate.now(), TransactionType.EXPENSE, TransactionStatus.CONFIRMED, 10L);

        Account account = accountOwnedByCurrentUser(10L);
        Transaction mapped = new Transaction();
        mapped.setType(TransactionType.EXPENSE);
        mapped.setStatus(TransactionStatus.CONFIRMED);
        mapped.setAmount(new BigDecimal("80.00"));

        when(accountService.findOwnedAccountOrThrow(10L)).thenReturn(account);
        when(transactionMapper.toEntity(dto)).thenReturn(mapped);
        when(transactionRepository.saveAndFlush(mapped)).thenReturn(mapped);
        when(transactionMapper.toResponseDTO(mapped)).thenReturn(
                new TransactionResponseDTO(1L, "Groceries", new BigDecimal("80.00"), LocalDate.now(), TransactionType.EXPENSE, TransactionStatus.CONFIRMED, 10L));

        transactionService.create(dto);

        verify(accountService).subtractBalance(10L, new BigDecimal("80.00"));
        verify(accountService, never()).addBalance(any(), any());
    }

    @Test
    void create_shouldNotTouchBalance_whenStatusIsPending() {
        CreateTransactionDTO dto = new CreateTransactionDTO(
                "Pending expense", new BigDecimal("80.00"), LocalDate.now(), TransactionType.EXPENSE, TransactionStatus.PENDING, 10L);

        Account account = accountOwnedByCurrentUser(10L);
        Transaction mapped = new Transaction();
        mapped.setType(TransactionType.EXPENSE);
        mapped.setStatus(TransactionStatus.PENDING);
        mapped.setAmount(new BigDecimal("80.00"));

        when(accountService.findOwnedAccountOrThrow(10L)).thenReturn(account);
        when(transactionMapper.toEntity(dto)).thenReturn(mapped);
        when(transactionRepository.saveAndFlush(mapped)).thenReturn(mapped);
        when(transactionMapper.toResponseDTO(mapped)).thenReturn(
                new TransactionResponseDTO(1L, "Pending expense", new BigDecimal("80.00"), LocalDate.now(), TransactionType.EXPENSE, TransactionStatus.PENDING, 10L));

        transactionService.create(dto);

        verify(accountService, never()).addBalance(any(), any());
        verify(accountService, never()).subtractBalance(any(), any());
    }

    @Test
    void create_shouldThrowNotFoundException_whenAccountNotOwnedByCurrentUser() {
        CreateTransactionDTO dto = new CreateTransactionDTO(
                "Salary", new BigDecimal("500.00"), LocalDate.now(), TransactionType.INCOME, TransactionStatus.CONFIRMED, 99L);

        when(accountService.findOwnedAccountOrThrow(99L)).thenThrow(new NotFoundException("Account not found with id: 99"));

        assertThatThrownBy(() -> transactionService.create(dto)).isInstanceOf(NotFoundException.class);
        verify(transactionRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_shouldRevertAndReapplyBalance_whenFinancialFieldsChange() {
        Account originalAccount = accountOwnedByCurrentUser(10L);
        Account newAccount = accountOwnedByCurrentUser(20L);

        Transaction existing = new Transaction();
        existing.setId(1L);
        existing.setAccount(originalAccount);
        existing.setType(TransactionType.EXPENSE);
        existing.setStatus(TransactionStatus.CONFIRMED);
        existing.setAmount(new BigDecimal("100.00"));
        existing.setDescription("Old");
        existing.setDate(LocalDate.now());

        UpdateTransactionDTO dto = new UpdateTransactionDTO(
                "New description", 20L, new BigDecimal("150.00"), LocalDate.now(), TransactionType.EXPENSE, TransactionStatus.CONFIRMED);

        when(accountService.findOwnedAccountOrThrow(20L)).thenReturn(newAccount);
        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(existing)).thenReturn(existing);
        when(transactionMapper.toResponseDTO(existing)).thenReturn(
                new TransactionResponseDTO(1L, "New description", new BigDecimal("150.00"), LocalDate.now(), TransactionType.EXPENSE, TransactionStatus.CONFIRMED, 20L));

        transactionService.update(1L, dto);

        // Reverting the old expense adds back to the original account...
        verify(accountService).addBalance(10L, new BigDecimal("100.00"));
        // ...and applying the new expense subtracts from the new account.
        verify(accountService).subtractBalance(20L, new BigDecimal("150.00"));
        assertThat(existing.getDescription()).isEqualTo("New description");
    }

    @Test
    void update_shouldNotTouchBalance_whenNoFinancialFieldsChange() {
        Account account = accountOwnedByCurrentUser(10L);

        Transaction existing = new Transaction();
        existing.setId(1L);
        existing.setAccount(account);
        existing.setType(TransactionType.EXPENSE);
        existing.setStatus(TransactionStatus.CONFIRMED);
        existing.setAmount(new BigDecimal("100.00"));
        existing.setDescription("Old");
        existing.setDate(LocalDate.now());

        UpdateTransactionDTO dto = new UpdateTransactionDTO(
                "Renamed only", 10L, new BigDecimal("100.00"), LocalDate.now(), TransactionType.EXPENSE, TransactionStatus.CONFIRMED);

        when(accountService.findOwnedAccountOrThrow(10L)).thenReturn(account);
        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(existing)).thenReturn(existing);
        when(transactionMapper.toResponseDTO(existing)).thenReturn(
                new TransactionResponseDTO(1L, "Renamed only", new BigDecimal("100.00"), LocalDate.now(), TransactionType.EXPENSE, TransactionStatus.CONFIRMED, 10L));

        transactionService.update(1L, dto);

        verify(accountService, never()).addBalance(any(), any());
        verify(accountService, never()).subtractBalance(any(), any());
    }

    @Test
    void deleteById_shouldRevertBalance_thenDeleteTransaction() {
        Account account = accountOwnedByCurrentUser(10L);

        Transaction existing = new Transaction();
        existing.setId(1L);
        existing.setAccount(account);
        existing.setType(TransactionType.INCOME);
        existing.setStatus(TransactionStatus.CONFIRMED);
        existing.setAmount(new BigDecimal("200.00"));

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));

        transactionService.deleteById(1L);

        verify(accountService).subtractBalance(10L, new BigDecimal("200.00"));
        verify(transactionRepository).delete(existing);
    }

    @Test
    void findById_shouldThrowNotFoundException_whenTransactionBelongsToAnotherUser() {
        User otherUser = new User();
        otherUser.setId(999L);
        Account account = new Account();
        account.setUser(otherUser);

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccount(account);

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionService.findById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void findAll_shouldReturnTransactionsForCurrentUser() {
        Transaction transaction = new Transaction();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Transaction> transactionsPage = new PageImpl<>(List.of(transaction), pageable, 1);
        TransactionResponseDTO dto = new TransactionResponseDTO(1L, "desc", BigDecimal.TEN, LocalDate.now(), TransactionType.INCOME, TransactionStatus.CONFIRMED, 1L);

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(transactionRepository.findAllByAccountUserId(CURRENT_USER_ID, pageable)).thenReturn(transactionsPage);
        when(transactionMapper.toResponseDTO(transaction)).thenReturn(dto);

        Page<TransactionResponseDTO> result = transactionService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @Test
    void confirm_shouldSetStatusConfirmedAndApplyBalance_whenCurrentlyPending() {
        Account account = accountOwnedByCurrentUser(10L);

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccount(account);
        transaction.setType(TransactionType.INCOME);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setAmount(new BigDecimal("300.00"));

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.toResponseDTO(transaction)).thenReturn(
                new TransactionResponseDTO(1L, "desc", new BigDecimal("300.00"), LocalDate.now(), TransactionType.INCOME, TransactionStatus.CONFIRMED, 10L));

        transactionService.confirm(1L);

        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.CONFIRMED);
        verify(accountService).addBalance(10L, new BigDecimal("300.00"));
    }

    @Test
    void confirm_shouldThrowBusinessRuleException_whenAlreadyConfirmed() {
        Account account = accountOwnedByCurrentUser(10L);

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccount(account);
        transaction.setStatus(TransactionStatus.CONFIRMED);

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionService.confirm(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Transaction is already confirmed");

        verify(accountService, never()).addBalance(any(), any());
        verify(transactionRepository, never()).save(any());
    }
}
