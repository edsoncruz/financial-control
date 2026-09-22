package com.cruz.financialcontrol.service;

import com.cruz.financialcontrol.exception.BusinessRuleException;
import com.cruz.financialcontrol.exception.NotFoundException;
import com.cruz.financialcontrol.model.dto.transaction.CreateTransactionDTO;
import com.cruz.financialcontrol.model.dto.transaction.TransactionResponseDTO;
import com.cruz.financialcontrol.model.dto.transfer.CreateTransferDTO;
import com.cruz.financialcontrol.model.dto.transfer.TransferResponseDTO;
import com.cruz.financialcontrol.model.dto.transfer.UpdateTransferDTO;
import com.cruz.financialcontrol.model.entity.Account;
import com.cruz.financialcontrol.model.entity.Transaction;
import com.cruz.financialcontrol.model.entity.Transfer;
import com.cruz.financialcontrol.model.entity.User;
import com.cruz.financialcontrol.model.enums.TransactionStatus;
import com.cruz.financialcontrol.model.enums.TransactionType;
import com.cruz.financialcontrol.model.mapper.TransferMapper;
import com.cruz.financialcontrol.repository.TransferRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;
    @Mock
    private TransactionService transactionService;
    @Mock
    private TransferMapper transferMapper;
    @Mock
    private SecurityUtils securityUtils;

    private TransferService transferService;

    private static final Long CURRENT_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(transferRepository, transactionService, transferMapper, securityUtils);
    }

    private Transaction transactionOwnedByCurrentUser(Long id) {
        User user = new User();
        user.setId(CURRENT_USER_ID);

        Account account = new Account();
        account.setUser(user);

        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setAccount(account);
        return transaction;
    }

    @Test
    void create_shouldThrowBusinessRuleException_whenOriginAndDestinationAreTheSameAccount() {
        CreateTransferDTO dto = new CreateTransferDTO(1L, 1L, BigDecimal.TEN, LocalDate.now());

        assertThatThrownBy(() -> transferService.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Origin and destination accounts must be different.");

        verifyNoInteractions(transactionService);
    }

    @Test
    void create_shouldCreatePairedExpenseAndIncomeTransactions_thenPersistTransfer() {
        CreateTransferDTO dto = new CreateTransferDTO(1L, 2L, new BigDecimal("100.00"), LocalDate.now());

        Transaction origin = transactionOwnedByCurrentUser(10L);
        Transaction destination = transactionOwnedByCurrentUser(20L);

        when(transactionService.createEntity(any(CreateTransactionDTO.class))).thenReturn(origin, destination);
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferMapper.toResponseDTO(any(Transfer.class))).thenReturn(
                new TransferResponseDTO(1L, mock(TransactionResponseDTO.class), mock(TransactionResponseDTO.class), new BigDecimal("100.00"), dto.date()));

        transferService.create(dto);

        ArgumentCaptor<CreateTransactionDTO> txCaptor = ArgumentCaptor.forClass(CreateTransactionDTO.class);
        verify(transactionService, times(2)).createEntity(txCaptor.capture());
        assertThat(txCaptor.getAllValues().get(0).type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(txCaptor.getAllValues().get(0).accountId()).isEqualTo(1L);
        assertThat(txCaptor.getAllValues().get(1).type()).isEqualTo(TransactionType.INCOME);
        assertThat(txCaptor.getAllValues().get(1).accountId()).isEqualTo(2L);

        ArgumentCaptor<Transfer> captor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(captor.capture());
        assertThat(captor.getValue().getOrigin()).isEqualTo(origin);
        assertThat(captor.getValue().getDestination()).isEqualTo(destination);
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void create_shouldThrowNotFoundException_whenEitherAccountNotOwnedByCurrentUser() {
        CreateTransferDTO dto = new CreateTransferDTO(1L, 2L, BigDecimal.TEN, LocalDate.now());

        when(transactionService.createEntity(any(CreateTransactionDTO.class)))
                .thenThrow(new NotFoundException("Account not found with id: 1"));

        assertThatThrownBy(() -> transferService.create(dto)).isInstanceOf(NotFoundException.class);
        verify(transferRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateBothLinkedTransactions_andTransferFields() {
        Transaction origin = transactionOwnedByCurrentUser(10L);
        origin.setType(TransactionType.EXPENSE);
        origin.setStatus(TransactionStatus.CONFIRMED);

        Transaction destination = transactionOwnedByCurrentUser(20L);
        destination.setType(TransactionType.INCOME);
        destination.setStatus(TransactionStatus.CONFIRMED);

        Transfer transfer = new Transfer();
        transfer.setId(1L);
        transfer.setOrigin(origin);
        transfer.setDestination(destination);
        transfer.setAmount(BigDecimal.TEN);
        transfer.setDate(LocalDate.now());

        UpdateTransferDTO dto = new UpdateTransferDTO(1L, 2L, new BigDecimal("250.00"), LocalDate.now());

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        when(transferRepository.save(transfer)).thenReturn(transfer);
        when(transferMapper.toResponseDTO(transfer)).thenReturn(
                new TransferResponseDTO(1L, mock(TransactionResponseDTO.class), mock(TransactionResponseDTO.class), new BigDecimal("250.00"), dto.date()));

        transferService.update(1L, dto);

        verify(transactionService).update(eq(10L), any());
        verify(transactionService).update(eq(20L), any());
        assertThat(transfer.getAmount()).isEqualByComparingTo("250.00");
    }

    @Test
    void deleteById_shouldDeleteTransferAndBothLinkedTransactions() {
        Transaction origin = transactionOwnedByCurrentUser(10L);
        Transaction destination = transactionOwnedByCurrentUser(20L);

        Transfer transfer = new Transfer();
        transfer.setId(1L);
        transfer.setOrigin(origin);
        transfer.setDestination(destination);

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        transferService.deleteById(1L);

        verify(transferRepository).delete(transfer);
        verify(transactionService).deleteById(10L);
        verify(transactionService).deleteById(20L);
    }

    @Test
    void findById_shouldThrowNotFoundException_whenTransferNotOwnedByCurrentUser() {
        User otherUser = new User();
        otherUser.setId(999L);
        Account otherAccount = new Account();
        otherAccount.setUser(otherUser);

        Transaction origin = new Transaction();
        origin.setAccount(otherAccount);

        Transfer transfer = new Transfer();
        transfer.setId(1L);
        transfer.setOrigin(origin);

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        assertThatThrownBy(() -> transferService.findById(1L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void findAll_shouldReturnTransfersForCurrentUser() {
        Transfer transfer = new Transfer();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Transfer> transfersPage = new PageImpl<>(List.of(transfer), pageable, 1);
        TransferResponseDTO dto = new TransferResponseDTO(1L, mock(TransactionResponseDTO.class), mock(TransactionResponseDTO.class), BigDecimal.TEN, LocalDate.now());

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(transferRepository.findAllByOriginAccountUserId(CURRENT_USER_ID, pageable)).thenReturn(transfersPage);
        when(transferMapper.toResponseDTO(transfer)).thenReturn(dto);

        Page<TransferResponseDTO> result = transferService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }
}
