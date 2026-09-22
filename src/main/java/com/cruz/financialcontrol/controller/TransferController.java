package com.cruz.financialcontrol.controller;

import com.cruz.financialcontrol.model.dto.transfer.CreateTransferDTO;
import com.cruz.financialcontrol.model.dto.transfer.TransferResponseDTO;
import com.cruz.financialcontrol.model.dto.transfer.UpdateTransferDTO;
import com.cruz.financialcontrol.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Class Name: TransferController
 * Description:
 *
 * @author edson
 * @date 14/09/2026
 */
@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponseDTO> create(@Valid @RequestBody CreateTransferDTO createTransferDTO) {
        TransferResponseDTO transferResponseDTO = transferService.create(createTransferDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(transferResponseDTO);
    }

    @PutMapping("{id}")
    public ResponseEntity<TransferResponseDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateTransferDTO updateTransferDTO) {
        TransferResponseDTO transferResponseDTO = transferService.update(id, updateTransferDTO);

        return ResponseEntity.ok(transferResponseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponseDTO> findById(@PathVariable Long id) {
        TransferResponseDTO transferResponseDTO = transferService.findById(id);

        return ResponseEntity.ok(transferResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transferService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<TransferResponseDTO>> findAll(@PageableDefault(size = 20, sort = "date") Pageable pageable) {
        Page<TransferResponseDTO> transferResponseDTOs = transferService.findAll(pageable);

        return ResponseEntity.ok(transferResponseDTOs);
    }
}