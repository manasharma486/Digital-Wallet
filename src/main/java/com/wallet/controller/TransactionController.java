package com.wallet.controller;

import com.wallet.dto.ApiResponse;
import com.wallet.dto.PageResponse;
import com.wallet.dto.transaction.TransactionResponse;
import com.wallet.dto.transaction.TransferRequest;
import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;
import com.wallet.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Endpoints for money transfers, transaction history, and searching")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money to another registered user's wallet")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        TransactionResponse response = transactionService.transfer(userDetails.getUsername(), request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer completed successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get paginated transaction history with filtering options")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<TransactionResponse> response = transactionService.getUserTransactions(
                userDetails.getUsername(), type, status, fromDate, toDate, pageable);

        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction details by ID")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        TransactionResponse response = transactionService.getTransactionById(id, userDetails.getUsername(), isAdmin);

        return ResponseEntity.ok(ApiResponse.success("Transaction details retrieved successfully", response));
    }
}
