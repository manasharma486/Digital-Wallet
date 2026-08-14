package com.wallet.repository;

import com.wallet.entity.Transaction;
import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Optional<Transaction> findByReference(String reference);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.sender.id = :userId OR t.receiver.id = :userId) " +
            "AND (:type IS NULL OR t.type = :type) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:fromDate IS NULL OR t.createdAt >= :fromDate) " +
            "AND (:toDate IS NULL OR t.createdAt <= :toDate)")
    Page<Transaction> findUserTransactions(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    @Query("SELECT t FROM Transaction t WHERE " +
            "(:type IS NULL OR t.type = :type) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:fromDate IS NULL OR t.createdAt >= :fromDate) " +
            "AND (:toDate IS NULL OR t.createdAt <= :toDate)")
    Page<Transaction> findAllTransactionsFiltered(
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    List<Transaction> findByStatusAndCreatedAtBefore(TransactionStatus status, LocalDateTime threshold);
}
