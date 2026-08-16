package com.wallet.controller;

import com.wallet.dto.ApiResponse;
import com.wallet.dto.PageResponse;
import com.wallet.dto.audit.AuditLogResponse;
import com.wallet.dto.transaction.TransactionResponse;
import com.wallet.dto.user.UserResponse;
import com.wallet.dto.wallet.WalletResponse;
import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;
import com.wallet.service.AdminService;
import com.wallet.service.AuditLogService;
import com.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin endpoints for wallet management, user management, and audit logs")
public class AdminController {

    private final AdminService adminService;
    private final WalletService walletService;
    private final AuditLogService auditLogService;

    public AdminController(AdminService adminService, WalletService walletService, AuditLogService auditLogService) {
        this.adminService = adminService;
        this.walletService = walletService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/users")
    @Operation(summary = "Get all registered users with pagination")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        PageResponse<UserResponse> response = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }

    @GetMapping("/wallets")
    @Operation(summary = "Get all wallets with pagination")
    public ResponseEntity<ApiResponse<PageResponse<WalletResponse>>> getWallets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        PageResponse<WalletResponse> response = adminService.getAllWallets(pageable);
        return ResponseEntity.ok(ApiResponse.success("Wallets retrieved successfully", response));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get all system transactions with filtering and pagination")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<TransactionResponse> response = adminService.getAllTransactions(type, status, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("System transactions retrieved successfully", response));
    }

    @PatchMapping("/wallets/{id}/freeze")
    @Operation(summary = "Freeze a user's wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> freezeWallet(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails adminDetails) {

        WalletResponse response = walletService.freezeWallet(id, adminDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Wallet frozen successfully", response));
    }

    @PatchMapping("/wallets/{id}/unfreeze")
    @Operation(summary = "Unfreeze a user's wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> unfreezeWallet(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails adminDetails) {

        WalletResponse response = walletService.unfreezeWallet(id, adminDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Wallet unfrozen successfully", response));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get system audit logs with pagination")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        PageResponse<AuditLogResponse> response = auditLogService.getAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", response));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get system statistics summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStats() {
        Map<String, Object> stats = adminService.getSystemStats();
        return ResponseEntity.ok(ApiResponse.success("System statistics retrieved successfully", stats));
    }
}
