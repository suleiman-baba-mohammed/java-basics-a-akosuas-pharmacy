package com.akosua.pharmacy.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * One line in the ledger. It holds ids and a name snapshot rather than live
 * Customer or Medication objects, so later changes to those objects cannot alter history.
 *
 * <p>The checks cover what a transaction can know about itself. That a prescription-only
 * dispense has a prescriber is enforced where transactions are created, in
 * {@code Pharmacy.dispense}, through {@link Medication#validateDispensing(String)}.
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
        Objects.requireNonNull(id, "Transaction id is required");
        Objects.requireNonNull(timestamp, "Transaction timestamp is required");
        Objects.requireNonNull(type, "Transaction type is required");
        Objects.requireNonNull(customerId, "Transaction customerId is required (use Optional.empty())");
        Objects.requireNonNull(medicationId, "Transaction medicationId is required");
        Objects.requireNonNull(medicationName, "Transaction medicationName is required");
        Objects.requireNonNull(total, "Transaction total is required");
        Objects.requireNonNull(prescriber, "Transaction prescriber is required (use Optional.empty())");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Transaction quantity must be positive");
        }
        switch (type) {
            case DISPENSE -> {
                if (customerId.isEmpty()) {
                    throw new IllegalArgumentException("A dispense must name the customer");
                }
                if (total.signum() <= 0) {
                    throw new IllegalArgumentException("A dispense total must be greater than zero");
                }
            }
            case RESTOCK -> {
                if (customerId.isPresent()) {
                    throw new IllegalArgumentException("A restock has no customer");
                }
                if (total.signum() != 0) {
                    throw new IllegalArgumentException("A restock total must be zero");
                }
            }
        }
    }
}
