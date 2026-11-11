package com.akosua.pharmacy.service;

import com.akosua.pharmacy.inventory.Inventory;
import com.akosua.pharmacy.ledger.Ledger;
import com.akosua.pharmacy.model.Customer;
import com.akosua.pharmacy.model.Medication;
import com.akosua.pharmacy.model.Transaction;
import com.akosua.pharmacy.model.TransactionType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Pharmacy {
    private final String name;
    private final Inventory inventory;
    private final Ledger ledger;
    private final Map<UUID, Customer> customers = new LinkedHashMap<>();

    public Pharmacy(String name, Inventory inventory, Ledger ledger) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Pharmacy name is required");
        this.name = name;
        this.inventory = Objects.requireNonNull(inventory);
        this.ledger = Objects.requireNonNull(ledger);
    }

    public void addMedication(Medication medication) { inventory.add(medication); }

    public void addCustomer(Customer customer) {
        Objects.requireNonNull(customer, "Customer is required");
        boolean duplicate = customers.values().stream()
                .anyMatch(existing -> existing.getName().equalsIgnoreCase(customer.getName()));
        if (duplicate) throw new IllegalArgumentException("Customer already exists: " + customer.getName());
        customers.put(customer.getId(), customer);
    }

    public void dispense(Customer customer, Medication medication, int quantity, String prescriber) {
        requireKnownCustomer(customer);
        requireKnownMedication(medication);
        medication.validateDispensing(prescriber);
        medication.dispenseStock(quantity);

        BigDecimal subtotal = medication.getPrice().multiply(BigDecimal.valueOf(quantity));
        BigDecimal total = customer.applyDiscount(subtotal).setScale(2, RoundingMode.HALF_UP);
        customer.recordDispense("Got " + quantity + " x " + medication.getName());

        ledger.record(new Transaction(UUID.randomUUID(), Instant.now(), TransactionType.DISPENSE,
                Optional.of(customer), medication, quantity, total,
                Optional.ofNullable(prescriber).filter(value -> !value.isBlank())));

        String prescriptionText = prescriber == null || prescriber.isBlank()
                ? "" : " (prescribed by " + prescriber + ")";
        System.out.printf("Dispensed %d x %s to %s%s for GHS %s%n",
                quantity, medication.getName(), customer.getName(), prescriptionText, total);
    }

    public void restock(Medication medication, int quantity) {
        requireKnownMedication(medication);
        medication.restock(quantity);
        ledger.record(new Transaction(UUID.randomUUID(), Instant.now(), TransactionType.RESTOCK,
                Optional.empty(), medication, quantity, BigDecimal.ZERO, Optional.empty()));
        System.out.println("Restocked " + quantity + " x " + medication.getName());
    }

    public List<Transaction> getTransactions() { return ledger.getTransactions(); }

    public void printReport() {
        System.out.println("\n=== " + name + " - end of day ===");
        System.out.println("Stock levels:");
        for (Medication medication : inventory.getAll()) {
            System.out.printf("  %s (%s): %d in stock%n",
                    medication.getName(), medication.getCategory(), medication.getStock());
        }
        System.out.println("Transactions today: " + ledger.size());
    }

    public void printLowStockAlerts() {
        List<Medication> lowStock = inventory.getLowStockMedications();
        System.out.println("\n=== Low-stock alerts ===");
        if (lowStock.isEmpty()) {
            System.out.println("No medicines are at or below their reorder levels.");
            return;
        }
        for (Medication medication : lowStock) {
            System.out.printf("LOW STOCK: %s — %d remaining (reorder level: %d)%n",
                    medication.getName(), medication.getStock(), medication.getReorderLevel());
        }
    }

    private void requireKnownCustomer(Customer customer) {
        Objects.requireNonNull(customer, "Customer is required");
        if (!customers.containsKey(customer.getId())) throw new IllegalArgumentException("Customer is not registered in this pharmacy");
    }
    private void requireKnownMedication(Medication medication) {
        Objects.requireNonNull(medication, "Medication is required");
        if (!inventory.contains(medication)) throw new IllegalArgumentException("Medication is not in this pharmacy's inventory");
    }
}
