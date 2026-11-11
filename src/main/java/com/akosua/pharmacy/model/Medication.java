package com.akosua.pharmacy.model;

import java.math.BigDecimal;
import java.util.UUID;
import com.akosua.pharmacy.exceptions.InsufficientStockException;

public abstract class Medication {
    private final UUID id = UUID.randomUUID();
    private final String name;
    private final BigDecimal price;
    private int stock;
    private final int reorderLevel;

    protected Medication(String name, BigDecimal price, int initialStock, int reorderLevel) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Medication name is required");
        if (price == null || price.signum() <= 0) throw new IllegalArgumentException("Price must be positive");
        if (initialStock < 0) throw new IllegalArgumentException("Starting stock cannot be negative");
        if (reorderLevel < 0) throw new IllegalArgumentException("Reorder level cannot be negative");
        this.name = name;
        this.price = price;
        this.stock = initialStock;
        this.reorderLevel = reorderLevel;
    }

    public abstract void validateDispensing(String prescriber);

    public final void dispenseStock(int quantity) {
        validatePositiveQuantity(quantity);
        if (quantity > stock) {
            throw new InsufficientStockException("Insufficient stock for " + name
                    + ": requested " + quantity + ", available " + stock);
        }
        stock -= quantity;
    }

    public final void restock(int quantity) {
        validatePositiveQuantity(quantity);
        stock = Math.addExact(stock, quantity);
    }

    private static void validatePositiveQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero");
    }

    public final UUID getId() { return id; }
    public final String getName() { return name; }
    public final BigDecimal getPrice() { return price; }
    public final int getStock() { return stock; }
    public final int getReorderLevel() { return reorderLevel; }
    public final boolean isLowStock() { return stock <= reorderLevel; }
    public abstract String getCategory();
}
