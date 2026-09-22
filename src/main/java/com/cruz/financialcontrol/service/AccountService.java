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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    /**
     * Creates a new account for the authenticated user.
     * @param createAccountDTO The DTO containing the account creation details.
     * @return The response DTO of the created account.
     * @throws BusinessRuleException if an account with the same name already exists for the authenticated user.
     */
    public AccountResponseDTO create(CreateAccountDTO createAccountDTO) {
        accountRepository.findByNameAndUserId(createAccountDTO.name(), securityUtils.getCurrentUserId()).ifPresent(account -> {
            throw new BusinessRuleException("Account with name '%s' already exists.".formatted(createAccountDTO.name()));
        });

        User authenticatedUser = userRepository.getReferenceById(securityUtils.getCurrentUserId());

        Account account = accountMapper.toEntity(createAccountDTO);
        account.setBalance(BigDecimal.ZERO);
        account.setUser(authenticatedUser);

        account = accountRepository.save(account);

        log.info("Account created with ID {}", account.getId());

        return accountMapper.toResponseDTO(account);
    }

    /**
     * Updates an existing account for the authenticated user.
     * @param updateAccountDTO The DTO containing the account update details.
     * @return The response DTO of the updated account.
     * @throws NotFoundException if the account does not exist or does not belong to the authenticated user.
     */
    public AccountResponseDTO update(Long id, UpdateAccountDTO updateAccountDTO) {
        Account account = findOwnedAccountOrThrow(id);
        account.setName(updateAccountDTO.name());

        account = accountRepository.save(account);

        log.info("Account updated with ID {}", account.getId());

        return accountMapper.toResponseDTO(account);
    }

    public void addBalance(Long accountId, BigDecimal balanceToAdd) {
        Account account = findOwnedAccountOrThrow(accountId);
        account.setBalance(account.getBalance().add(balanceToAdd));

        accountRepository.save(account);

        log.info("Added {} to account with ID {}", balanceToAdd, account.getId());
    }

    public void subtractBalance(Long accountId, BigDecimal balanceToSubtract) {
        Account account = findOwnedAccountOrThrow(accountId);
        account.setBalance(account.getBalance().subtract(balanceToSubtract));

        accountRepository.save(account);

        log.info("Subtracted {} from account with ID {}", balanceToSubtract, account.getId());
    }

    /**
     * Deletes an account by its ID for the authenticated user.
     * @param id The ID of the account to be deleted.
     * @throws NotFoundException if the account does not exist or does not belong to the authenticated user.
     */
    public void deleteById(Long id) {
        Account account = findOwnedAccountOrThrow(id);
        accountRepository.delete(account);

        log.info("Account deleted with ID {}", account.getId());
    }

    /**
     * Finds an account by its ID for the authenticated user.
     * @param id The ID of the account to be found.
     * @return The response DTO of the found account.
     * @throws NotFoundException if the account does not exist or does not belong to the authenticated user.
     */
    public AccountResponseDTO findById(Long id) {
        Account account = findOwnedAccountOrThrow(id);

        return accountMapper.toResponseDTO(account);
    }

    /**
     * Finds all accounts for the authenticated user, paginated.
     * @param pageable Pagination and sorting information.
     * @return A page of response DTOs of the found accounts.
     */
    public Page<AccountResponseDTO> findAll(Pageable pageable) {
        Page<Account> accounts = accountRepository.findAllByUserId(securityUtils.getCurrentUserId(), pageable);
        return accounts.map(accountMapper::toResponseDTO);
    }

    /**
     * Finds an account by its ID and the authenticated user's ID.
     * @param id The ID of the account to be found.
     * @return The found account entity.
     * @throws NotFoundException if the account does not exist or does not belong to the authenticated user.
     */
    public Account findOwnedAccountOrThrow(Long id) {

        return accountRepository
                .findByIdAndUserId(id, securityUtils.getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("Account not found with id: %s ".formatted(id)));
    }
}
