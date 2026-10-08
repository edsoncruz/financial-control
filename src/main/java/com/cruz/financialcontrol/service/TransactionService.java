package com.cruz.financialcontrol.service;

import com.cruz.financialcontrol.exception.BusinessRuleException;
import com.cruz.financialcontrol.exception.NotFoundException;
import com.cruz.financialcontrol.model.dto.transaction.CreateTransactionDTO;
import com.cruz.financialcontrol.model.dto.transaction.TransactionResponseDTO;
import com.cruz.financialcontrol.model.dto.transaction.UpdateTransactionDTO;
import com.cruz.financialcontrol.model.entity.Account;
import com.cruz.financialcontrol.model.entity.Transaction;
import com.cruz.financialcontrol.model.entity.TransactionCategory;
import com.cruz.financialcontrol.model.enums.TransactionStatus;
import com.cruz.financialcontrol.model.mapper.TransactionMapper;
import com.cruz.financialcontrol.repository.TransactionRepository;
import com.cruz.financialcontrol.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final TransactionMapper transactionMapper;
    private final TransactionCategoryService transactionCategoryService;
    private final SecurityUtils securityUtils;

    /**
     * Creates a new transaction entity for the authenticated user and persists it.
     * Package-private: intended for use by other services in this package (e.g. TransferService)
     * that need the managed entity itself, not just its response DTO.
     * @param createTransactionDTO The DTO containing the transaction creation details.
     * @return The persisted transaction entity.
     * @throws NotFoundException if the account does not exist or does not belong to the authenticated user.
     */
    @Transactional
    Transaction createEntity(CreateTransactionDTO createTransactionDTO) {
        Account account = accountService.findOwnedAccountOrThrow(createTransactionDTO.accountId());
        TransactionCategory category = transactionCategoryService.findEntityById(createTransactionDTO.transactionCategoryId());

        if (!category.getTransactionType().equals(createTransactionDTO.type()))
            throw new BusinessRuleException("Transaction category type match the transaction category type");

        Transaction transaction = transactionMapper.toEntity(createTransactionDTO);
        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction = transactionRepository.saveAndFlush(transaction);

        updateBalance(transaction);

        log.info("Transaction created with ID {}", transaction.getId());

        return transaction;
    }

    /**
     * Creates a new transaction for the authenticated user.
     * @param createTransactionDTO The DTO containing the transaction creation details.
     * @return The response DTO of the created transaction.
     * @throws NotFoundException if the account does not exist or does not belong to the authenticated user.
     */
    @Transactional
    public TransactionResponseDTO create(CreateTransactionDTO createTransactionDTO) {
        return transactionMapper.toResponseDTO(createEntity(createTransactionDTO));
    }

    /**
     * Updates an existing transaction for the authenticated user.
     * @param updateTransactionDTO The DTO containing the transaction update details.
     * @return The response DTO of the updated transaction.
     * @throws NotFoundException if the transaction does not exist or does not belong to the authenticated user.
     */
    @Transactional
    public TransactionResponseDTO update(Long id, UpdateTransactionDTO updateTransactionDTO) {

        Account account = accountService.findOwnedAccountOrThrow(updateTransactionDTO.accountId());
        TransactionCategory category = transactionCategoryService.findEntityById(updateTransactionDTO.transactionCategoryId());

        if (!category.getTransactionType().equals(updateTransactionDTO.type()))
            throw new BusinessRuleException("Transaction category type match the transaction category type");

        Transaction transaction = findOwnedTransactionOrThrow(id);

        if(!updateTransactionDTO.accountId().equals(transaction.getAccount().getId()) ||
           !updateTransactionDTO.type().equals(transaction.getType()) ||
           updateTransactionDTO.amount().compareTo(transaction.getAmount()) != 0 ||
           !updateTransactionDTO.status().equals(transaction.getStatus())
        ) {
            revertBalance(transaction);

            transaction.setAccount(account);
            transaction.setType(updateTransactionDTO.type());
            transaction.setAmount(updateTransactionDTO.amount());
            transaction.setStatus(updateTransactionDTO.status());

            updateBalance(transaction);
        }

        transaction.setDescription(updateTransactionDTO.description());
        transaction.setDate(updateTransactionDTO.date());
        transaction.setCategory(category);

        transaction = transactionRepository.save(transaction);

        log.info("Transaction updated with ID {}", transaction.getId());

        return transactionMapper.toResponseDTO(transaction);
    }

    /**
     * Deletes a transaction by its ID for the authenticated user.
     * @param id The ID of the transaction to be deleted.
     * @throws NotFoundException if the transaction does not exist or does not belong to the authenticated user.
     */
    @Transactional
    public void deleteById(Long id) {
        Transaction transaction = findOwnedTransactionOrThrow(id);
        revertBalance(transaction);
        transactionRepository.delete(transaction);

        log.info("Transaction deleted with ID {}", transaction.getId());
    }

    /**
     * Finds a transaction by its ID for the authenticated user.
     * @param id The ID of the transaction to be found.
     * @return The response DTO of the found transaction.
     * @throws NotFoundException if the transaction does not exist or does not belong to the authenticated user.
     */
    public TransactionResponseDTO findById(Long id) {
        Transaction transaction = findOwnedTransactionOrThrow(id);

        return transactionMapper.toResponseDTO(transaction);
    }

    /**
     * Finds all transactions for the authenticated user, paginated.
     * @param pageable Pagination and sorting information.
     * @return A page of response DTOs of the found transactions.
     */
    public Page<TransactionResponseDTO> findAll(Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findAllByAccountUserId(securityUtils.getCurrentUserId(), pageable);
        return transactions.map(transactionMapper::toResponseDTO);
    }

    /**
     * Finds a transaction by its ID for the authenticated user.
     * @param id The ID of the transaction to be found.
     * @return The found transaction entity.
     * @throws NotFoundException if the transaction does not exist or does not belong to the authenticated user.
     */
    private Transaction findOwnedTransactionOrThrow(Long id) {
        Transaction transaction = transactionRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: %s".formatted(id)));

        if (!Objects.equals(transaction.getAccount().getUser().getId(), securityUtils.getCurrentUserId())) {
            throw new NotFoundException("Transaction not found with id: %s".formatted(id));
        }

        return transaction;
    }

    /**
     * Updates the balance of the account associated with the transaction based on its type and status.
     * @param transaction The transaction for which the account balance needs to be updated.
     */
    private void updateBalance(Transaction transaction) {
        if (transaction.getType().isIncome() && transaction.getStatus().isConfirmed()) {
            accountService.addBalance(transaction.getAccount().getId(), transaction.getAmount());
        } else if (transaction.getStatus().isConfirmed()) {
            accountService.subtractBalance(transaction.getAccount().getId(), transaction.getAmount());
        }
    }

    /**
     * Reverts the balance of the account associated with the transaction based on its type and status.
     * @param transaction The transaction for which the account balance needs to be reverted.
     */
    private void revertBalance(Transaction transaction) {
        if (transaction.getType().isIncome() && transaction.getStatus().isConfirmed()) {
            accountService.subtractBalance(transaction.getAccount().getId(), transaction.getAmount());
        } else if (transaction.getStatus().isConfirmed()) {
            accountService.addBalance(transaction.getAccount().getId(), transaction.getAmount());
        }
    }

    @Transactional
    public TransactionResponseDTO confirm(Long id) {
        Transaction transaction = findOwnedTransactionOrThrow(id);

        if(transaction.getStatus().isConfirmed()) {
            throw new BusinessRuleException("Transaction is already confirmed");
        }

        transaction.setStatus(TransactionStatus.CONFIRMED);
        transaction = transactionRepository.save(transaction);

        updateBalance(transaction);

        log.info("Transaction confirmed with ID {}", transaction.getId());

        return transactionMapper.toResponseDTO(transaction);
    }
}

