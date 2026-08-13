package com.wallet.controller;

import com.wallet.dto.ApiResponse;
import com.wallet.dto.wallet.DepositRequest;
import com.wallet.dto.wallet.WalletResponse;
import com.wallet.dto.wallet.WithdrawRequest;
import com.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@Tag(name = "Wallet", description = "Endpoints for wallet balance management, deposit, and withdrawal")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user's wallet details and balance")
    public ResponseEntity<ApiResponse<WalletResponse>> getMyWallet(@AuthenticationPrincipal UserDetails userDetails) {
        WalletResponse response = walletService.getWalletByUserEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Wallet details retrieved successfully", response));
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds into user's wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> deposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DepositRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        WalletResponse response = walletService.deposit(userDetails.getUsername(), request.getAmount(), idempotencyKey);
        return ResponseEntity.ok(ApiResponse.success("Deposit completed successfully", response));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds from user's wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> withdraw(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WithdrawRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        WalletResponse response = walletService.withdraw(userDetails.getUsername(), request.getAmount(), idempotencyKey);
        return ResponseEntity.ok(ApiResponse.success("Withdrawal completed successfully", response));
    }
}
