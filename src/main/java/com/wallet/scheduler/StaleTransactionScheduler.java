package com.wallet.scheduler;

import com.wallet.entity.Transaction;
import com.wallet.enums.TransactionStatus;
import com.wallet.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class StaleTransactionScheduler {

    private static final Logger log = LoggerFactory.getLogger(StaleTransactionScheduler.class);

    private final TransactionRepository transactionRepository;

    public StaleTransactionScheduler(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Runs every hour to clean up stale PENDING transactions older than 24 hours.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupStaleTransactions() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<Transaction> staleTransactions = transactionRepository
                .findByStatusAndCreatedAtBefore(TransactionStatus.PENDING, threshold);

        if (!staleTransactions.isEmpty()) {
            log.info("Found {} stale PENDING transactions. Marking as FAILED...", staleTransactions.size());
            for (Transaction txn : staleTransactions) {
                txn.setStatus(TransactionStatus.FAILED);
                txn.setDescription(txn.getDescription() + " (Timed out after 24h PENDING)");
            }
            transactionRepository.saveAll(staleTransactions);
            log.info("Successfully marked {} stale transactions as FAILED.", staleTransactions.size());
        }
    }
}
