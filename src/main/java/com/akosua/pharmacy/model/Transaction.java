package com.akosua.pharmacy.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * One line in the ledger. It holds ids and a name snapshot rather than live
 * Customer or Medication objects, so later changes to those objects cannot alter history.
 */
public record Transaction(
        UUID id,
        Instant timestamp,
        TransactionType type,
        Optional<UUID> customerId,
        UUID medicationId,
        String medicationName,
        int quantity,
        BigDecimal total,
        Optional<String> prescriber) {

    public Transaction {
        if (id == null || timestamp == null || type == null || customerId == null
                || medicationId == null || medicationName == null || total == null
                || prescriber == null) {
            throw new IllegalArgumentException("Transaction fields cannot be null");
        }
        if (quantity <= 0) throw new IllegalArgumentException("Transaction quantity must be positive");
        if (total.signum() < 0) throw new IllegalArgumentException("Transaction total cannot be negative");
    }
}
