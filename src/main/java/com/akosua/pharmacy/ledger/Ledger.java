package com.akosua.pharmacy.ledger;

import com.akosua.pharmacy.model.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Ledger {
    private final List<Transaction> transactions = new ArrayList<>();

    public void record(Transaction transaction) {
        transactions.add(Objects.requireNonNull(transaction, "Transaction is required"));
    }

    public List<Transaction> getTransactions() { return List.copyOf(transactions); }
    public int size() { return transactions.size(); }
}
