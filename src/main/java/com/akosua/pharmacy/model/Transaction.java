package com.akosua.pharmacy.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record Transaction(
        UUID id,
        Instant timestamp,
        TransactionType type,
        Optional<Customer> customer,
        Medication medication,
        int quantity,
        BigDecimal total,
        Optional<String> prescriber) {

    public Transaction {
        if (id == null || timestamp == null || type == null || customer == null
                || medication == null || total == null || prescriber == null) {
            throw new IllegalArgumentException("Transaction fields cannot be null");
        }
        if (quantity <= 0) throw new IllegalArgumentException("Transaction quantity must be positive");
        if (total.signum() < 0) throw new IllegalArgumentException("Transaction total cannot be negative");
    }
}
