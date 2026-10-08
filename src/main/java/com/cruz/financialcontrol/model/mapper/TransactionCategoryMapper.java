package com.cruz.financialcontrol.model.mapper;

import com.cruz.financialcontrol.model.dto.transactioncategory.CreateTransactionCategoryDTO;
import com.cruz.financialcontrol.model.dto.transactioncategory.TransactionCategoryResponseDTO;
import com.cruz.financialcontrol.model.entity.TransactionCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionCategoryMapper {

    @IgnoreAuditFields
    TransactionCategory toEntity(CreateTransactionCategoryDTO dto);

    TransactionCategoryResponseDTO toResponseDTO(TransactionCategory entity);
}
