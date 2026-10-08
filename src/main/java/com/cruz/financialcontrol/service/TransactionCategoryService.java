package com.cruz.financialcontrol.service;

import com.cruz.financialcontrol.exception.NotFoundException;
import com.cruz.financialcontrol.model.dto.transactioncategory.CreateTransactionCategoryDTO;
import com.cruz.financialcontrol.model.dto.transactioncategory.TransactionCategoryResponseDTO;
import com.cruz.financialcontrol.model.dto.transactioncategory.UpdateTransactionCategoryDTO;
import com.cruz.financialcontrol.model.entity.TransactionCategory;
import com.cruz.financialcontrol.model.mapper.TransactionCategoryMapper;
import com.cruz.financialcontrol.repository.TransactionCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Class Name: TransactionCategoryService
 * Description:
 *
 * @author edson
 * @date 07/10/2026
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionCategoryService {

    private final TransactionCategoryRepository transactionCategoryRepository;
    private final TransactionCategoryMapper transactionCategoryMapper;

    public TransactionCategoryResponseDTO create(CreateTransactionCategoryDTO createDTO) {
        TransactionCategory category = transactionCategoryMapper.toEntity(createDTO);
        category = transactionCategoryRepository.save(category);

        log.info("Transaction category created with ID {}", category.getId());

        return transactionCategoryMapper.toResponseDTO(category);
    }

    public TransactionCategoryResponseDTO update(Long id, UpdateTransactionCategoryDTO updateDTO) {
        TransactionCategory category = findCategoryOrThrow(id);
        category.setDescription(updateDTO.description());
        category.setTransactionType(updateDTO.transactionType());
        category = transactionCategoryRepository.save(category);

        log.info("Transaction category updated with ID {}", category.getId());

        return transactionCategoryMapper.toResponseDTO(category);
    }

    public void deleteById(Long id) {
        TransactionCategory category = findCategoryOrThrow(id);
        transactionCategoryRepository.delete(category);

        log.info("Transaction category deleted with ID {}", category.getId());
    }

    public TransactionCategoryResponseDTO findById(Long id) {
        return transactionCategoryMapper.toResponseDTO(findCategoryOrThrow(id));
    }

    TransactionCategory findEntityById(Long id) {
        return transactionCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Transaction category not found with id: %s".formatted(id)));
    }


    @Cacheable(
            cacheNames = "transaction-categories",
            key = "#pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()"
    )
    public Page<TransactionCategoryResponseDTO> findAll(Pageable pageable) {
        return transactionCategoryRepository.findAll(pageable)
                .map(transactionCategoryMapper::toResponseDTO);
    }

    private TransactionCategory findCategoryOrThrow(Long id) {
        return transactionCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Transaction category not found with id: %s".formatted(id)));
    }

    public TransactionCategoryResponseDTO findByDescription(String description) {
        return transactionCategoryRepository.findByDescription(description)
                .map(transactionCategoryMapper::toResponseDTO)
                .orElseThrow(() -> new NotFoundException(
                        "Transaction category not found with description: %s".formatted(description)));

    }
}
