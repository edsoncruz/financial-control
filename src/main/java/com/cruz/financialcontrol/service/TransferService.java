package com.cruz.financialcontrol.service;

import com.cruz.financialcontrol.exception.BusinessRuleException;
import com.cruz.financialcontrol.exception.NotFoundException;
import com.cruz.financialcontrol.model.dto.transaction.CreateTransactionDTO;
import com.cruz.financialcontrol.model.dto.transaction.UpdateTransactionDTO;
import com.cruz.financialcontrol.model.dto.transactioncategory.TransactionCategoryResponseDTO;
import com.cruz.financialcontrol.model.dto.transfer.CreateTransferDTO;
import com.cruz.financialcontrol.model.dto.transfer.TransferResponseDTO;
import com.cruz.financialcontrol.model.dto.transfer.UpdateTransferDTO;
import com.cruz.financialcontrol.model.entity.Transaction;
import com.cruz.financialcontrol.model.entity.Transfer;
import com.cruz.financialcontrol.model.enums.TransactionStatus;
import com.cruz.financialcontrol.model.enums.TransactionType;
import com.cruz.financialcontrol.model.mapper.TransferMapper;
import com.cruz.financialcontrol.repository.TransferRepository;
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
public class TransferService {

    private final TransferRepository transferRepository;
    private final TransactionService transactionService;
    private final TransactionCategoryService transactionCategoryService;
    private final TransferMapper transferMapper;
    private final SecurityUtils securityUtils;

    private static final String TRANSFER_TO_DESCRIPTION = "Transfer to account %s";
    private static final String TRANSFER_FROM_DESCRIPTION = "Transfer from account %s";

    /**
     * Transfers an amount from one account to another for the authenticated user.
     * Creates a paired expense (origin) and income (destination) transaction, both already completed.
     * @param createTransferDTO The DTO containing the transfer details.
     * @return The response DTO of the created transfer.
     * @throws BusinessRuleException if the origin and destination accounts are the same.
     * @throws NotFoundException if either account does not exist or does not belong to the authenticated user.
     */
    @Transactional
    public TransferResponseDTO create(CreateTransferDTO createTransferDTO) {
        if (createTransferDTO.originAccountId().equals(createTransferDTO.destinationAccountId())) {
            throw new BusinessRuleException("Origin and destination accounts must be different.");
        }

        TransactionCategoryResponseDTO transferOut = transactionCategoryService.findByDescription("Transfer out");
        TransactionCategoryResponseDTO transferIn = transactionCategoryService.findByDescription("Transfer in");

        Transaction originTransaction = transactionService.createEntity(new CreateTransactionDTO(
                TRANSFER_TO_DESCRIPTION.formatted(createTransferDTO.destinationAccountId()),
                createTransferDTO.amount(),
                createTransferDTO.date(),
                TransactionType.EXPENSE,
                TransactionStatus.CONFIRMED,
                createTransferDTO.originAccountId(),
                transferOut.id()
        ));

        Transaction destinationTransaction = transactionService.createEntity(new CreateTransactionDTO(
                TRANSFER_FROM_DESCRIPTION.formatted(createTransferDTO.originAccountId()),
                createTransferDTO.amount(),
                createTransferDTO.date(),
                TransactionType.INCOME,
                TransactionStatus.CONFIRMED,
                createTransferDTO.destinationAccountId(),
                transferIn.id()
        ));

        Transfer transfer = new Transfer();
        transfer.setOrigin(originTransaction);
        transfer.setDestination(destinationTransaction);
        transfer.setAmount(createTransferDTO.amount());
        transfer.setDate(createTransferDTO.date());

        transfer = transferRepository.save(transfer);

        log.info("Transfer created with ID: {}", transfer.getId());

        return transferMapper.toResponseDTO(transfer);
    }

    @Transactional
    public TransferResponseDTO update(Long id, UpdateTransferDTO updateTransferDTO) {
        Transfer transfer = findOwnedTransferOrThrow(id);

        TransactionCategoryResponseDTO transferOut = transactionCategoryService.findByDescription("Transfer out");
        TransactionCategoryResponseDTO transferIn = transactionCategoryService.findByDescription("Transfer in");

        transactionService.update(transfer.getOrigin().getId(), new UpdateTransactionDTO(
                        TRANSFER_TO_DESCRIPTION.formatted(updateTransferDTO.destinationAccountId()),
                        updateTransferDTO.originAccountId(),
                        updateTransferDTO.amount(),
                        updateTransferDTO.date(),
                        transfer.getOrigin().getType(),
                        transfer.getOrigin().getStatus(),
                        transferOut.id()
                )
        );

        transactionService.update(transfer.getDestination().getId(), new UpdateTransactionDTO(
                        TRANSFER_FROM_DESCRIPTION.formatted(updateTransferDTO.originAccountId()),
                        updateTransferDTO.destinationAccountId(),
                        updateTransferDTO.amount(),
                        updateTransferDTO.date(),
                        transfer.getDestination().getType(),
                        transfer.getDestination().getStatus(),
                        transferIn.id()
                )
        );

        transfer.setAmount(updateTransferDTO.amount());
        transfer.setDate(updateTransferDTO.date());

        transfer = transferRepository.save(transfer);

        log.info("Transfer updated with ID: {}", transfer.getId());

        return transferMapper.toResponseDTO(transfer);
    }

    /**
     * Deletes a transfer by its ID for the authenticated user.
     * This method also deletes the associated origin and destination transactions.
     * @param id The ID of the transfer to be deleted.
     * @throws NotFoundException if the transfer does not exist or does not belong to the authenticated user.
     */
    @Transactional
    public void deleteById(Long id) {
        Transfer transfer = findOwnedTransferOrThrow(id);

        transferRepository.delete(transfer);

        transactionService.deleteById(transfer.getOrigin().getId());
        transactionService.deleteById(transfer.getDestination().getId());

        log.info("Transfer deleted with ID: {}", transfer.getId());
    }

    /**
     * Finds a transfer by its ID for the authenticated user.
     * @param id The ID of the transfer to be found.
     * @return The response DTO of the found transfer.
     * @throws NotFoundException if the transfer does not exist or does not belong to the authenticated user.
     */
    public TransferResponseDTO findById(Long id) {
        Transfer transfer = findOwnedTransferOrThrow(id);

        return transferMapper.toResponseDTO(transfer);
    }

    /**
     * Finds all transfers for the authenticated user, paginated.
     * @param pageable Pagination and sorting information.
     * @return A page of response DTOs of the found transfers.
     */
    public Page<TransferResponseDTO> findAll(Pageable pageable) {
        Page<Transfer> transfers = transferRepository.findAllByOriginAccountUserId(securityUtils.getCurrentUserId(), pageable);

        return transfers.map(transferMapper::toResponseDTO);
    }


    /**
     * Finds a transfer by its ID for the authenticated user.
     * @param id The ID of the transfer to be found.
     * @return The found transfer entity.
     * @throws NotFoundException if the transfer does not exist or does not belong to the authenticated user.
     */
    private Transfer findOwnedTransferOrThrow(Long id) {
        Transfer transfer = transferRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Transfer not found with id: %s".formatted(id)));

        if (!Objects.equals(transfer.getOrigin().getAccount().getUser().getId(), securityUtils.getCurrentUserId())) {
            throw new NotFoundException("Transfer not found with id: %s".formatted(id));
        }

        return transfer;
    }
}

