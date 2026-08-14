package com.wallet.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ReferenceGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String generateTransactionReference() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String randomHex = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "TXN-" + dateStr + "-" + randomHex;
    }
}
