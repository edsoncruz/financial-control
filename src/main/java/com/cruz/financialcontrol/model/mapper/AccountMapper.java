package com.cruz.financialcontrol.model.mapper;

import com.cruz.financialcontrol.model.dto.account.AccountResponseDTO;
import com.cruz.financialcontrol.model.dto.account.CreateAccountDTO;
import com.cruz.financialcontrol.model.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @IgnoreAuditFields
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "version", ignore = true)
    Account toEntity(CreateAccountDTO dto);

    AccountResponseDTO toResponseDTO(Account entity);

    List<AccountResponseDTO> toResponseDTO(List<Account> entities);
}
