package com.cruz.financialcontrol.service;

import com.cruz.financialcontrol.exception.BusinessRuleException;
import com.cruz.financialcontrol.exception.NotFoundException;
import com.cruz.financialcontrol.model.dto.account.AccountResponseDTO;
import com.cruz.financialcontrol.model.dto.account.CreateAccountDTO;
import com.cruz.financialcontrol.model.dto.account.UpdateAccountDTO;
import com.cruz.financialcontrol.model.entity.Account;
import com.cruz.financialcontrol.model.entity.User;
import com.cruz.financialcontrol.model.mapper.AccountMapper;
import com.cruz.financialcontrol.repository.AccountRepository;
import com.cruz.financialcontrol.repository.UserRepository;
import com.cruz.financialcontrol.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityUtils securityUtils;

    private AccountService accountService;

    private static final Long CURRENT_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, accountMapper, userRepository, securityUtils);
    }

    @Test
    void create_shouldPersistAccountWithZeroBalance_whenNameIsAvailable() {
        CreateAccountDTO dto = new CreateAccountDTO("Savings");
        User currentUser = new User();
        currentUser.setId(CURRENT_USER_ID);

        Account mappedAccount = new Account();
        mappedAccount.setName("Savings");

        when(accountRepository.findByNameAndUserId("Savings", CURRENT_USER_ID)).thenReturn(Optional.empty());
        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(userRepository.getReferenceById(CURRENT_USER_ID)).thenReturn(currentUser);
        when(accountMapper.toEntity(dto)).thenReturn(mappedAccount);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountMapper.toResponseDTO(any(Account.class)))
                .thenReturn(new AccountResponseDTO(1L, "Savings", BigDecimal.ZERO));

        AccountResponseDTO response = accountService.create(dto);

        assertThat(response.name()).isEqualTo("Savings");

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(captor.getValue().getUser()).isEqualTo(currentUser);
    }

    @Test
    void create_shouldThrowBusinessRuleException_whenAccountNameAlreadyExists() {
        CreateAccountDTO dto = new CreateAccountDTO("Savings");
        when(accountRepository.findByNameAndUserId("Savings", CURRENT_USER_ID)).thenReturn(Optional.of(new Account()));

        assertThatThrownBy(() -> accountService.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Account with name 'Savings' already exists.");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void update_shouldChangeName_whenAccountOwnedByCurrentUser() {
        Account existing = new Account();
        existing.setId(1L);
        existing.setName("Old Name");
        existing.setBalance(BigDecimal.TEN);

        UpdateAccountDTO dto = new UpdateAccountDTO("New Name");

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(accountRepository.findByIdAndUserId(1L, CURRENT_USER_ID)).thenReturn(Optional.of(existing));
        when(accountRepository.save(existing)).thenReturn(existing);
        when(accountMapper.toResponseDTO(existing)).thenReturn(new AccountResponseDTO(1L, "New Name", BigDecimal.TEN));

        AccountResponseDTO response = accountService.update(1L, dto);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(existing.getName()).isEqualTo("New Name");
    }

    @Test
    void update_shouldThrowNotFoundException_whenAccountNotOwnedByCurrentUser() {
        UpdateAccountDTO dto = new UpdateAccountDTO("New Name");
        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(accountRepository.findByIdAndUserId(99L, CURRENT_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.update(99L, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void addBalance_shouldIncreaseAccountBalance() {
        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("100.00"));

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(accountRepository.findByIdAndUserId(1L, CURRENT_USER_ID)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        accountService.addBalance(1L, new BigDecimal("50.00"));

        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void subtractBalance_shouldDecreaseAccountBalance() {
        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("100.00"));

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(accountRepository.findByIdAndUserId(1L, CURRENT_USER_ID)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        accountService.subtractBalance(1L, new BigDecimal("30.00"));

        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    void deleteById_shouldDeleteAccount_whenOwnedByCurrentUser() {
        Account account = new Account();
        account.setId(1L);

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(accountRepository.findByIdAndUserId(1L, CURRENT_USER_ID)).thenReturn(Optional.of(account));

        accountService.deleteById(1L);

        verify(accountRepository).delete(account);
    }

    @Test
    void deleteById_shouldThrowNotFoundException_whenAccountNotOwned() {
        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(accountRepository.findByIdAndUserId(1L, CURRENT_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deleteById(1L))
                .isInstanceOf(NotFoundException.class);

        verify(accountRepository, never()).delete(any());
    }

    @Test
    void findById_shouldReturnAccount_whenOwnedByCurrentUser() {
        Account account = new Account();
        account.setId(1L);

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(accountRepository.findByIdAndUserId(1L, CURRENT_USER_ID)).thenReturn(Optional.of(account));
        when(accountMapper.toResponseDTO(account)).thenReturn(new AccountResponseDTO(1L, "Savings", BigDecimal.ZERO));

        AccountResponseDTO response = accountService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void findAll_shouldReturnAllAccountsBelongingToCurrentUser() {
        Account account1 = new Account();
        Account account2 = new Account();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Account> accountsPage = new PageImpl<>(List.of(account1, account2), pageable, 2);

        AccountResponseDTO dto1 = new AccountResponseDTO(1L, "A", BigDecimal.ZERO);
        AccountResponseDTO dto2 = new AccountResponseDTO(2L, "B", BigDecimal.ZERO);

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(accountRepository.findAllByUserId(CURRENT_USER_ID, pageable)).thenReturn(accountsPage);
        when(accountMapper.toResponseDTO(account1)).thenReturn(dto1);
        when(accountMapper.toResponseDTO(account2)).thenReturn(dto2);

        Page<AccountResponseDTO> response = accountService.findAll(pageable);

        assertThat(response.getContent()).containsExactly(dto1, dto2);
    }
}
