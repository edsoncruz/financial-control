package com.cruz.financialcontrol.model.mapper;

import com.cruz.financialcontrol.model.dto.transaction.CreateTransactionDTO;
import com.cruz.financialcontrol.model.dto.transaction.TransactionResponseDTO;
import com.cruz.financialcontrol.model.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @IgnoreAuditFields
    @Mapping(source = "accountId", target = "account.id")
    Transaction toEntity(CreateTransactionDTO dto);

    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "category", target = "categoryResponseDTO")
    TransactionResponseDTO toResponseDTO(Transaction entity);

    List<TransactionResponseDTO> toResponseDTO(List<Transaction> entities);
}
