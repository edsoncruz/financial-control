package com.cruz.financialcontrol.model.mapper;

import com.cruz.financialcontrol.model.dto.transfer.TransferResponseDTO;
import com.cruz.financialcontrol.model.entity.Transfer;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = TransactionMapper.class)
public interface TransferMapper {

    TransferResponseDTO toResponseDTO(Transfer entity);

    List<TransferResponseDTO> toResponseDTO(List<Transfer> entities);
}
