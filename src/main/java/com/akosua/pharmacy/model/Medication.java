package com.akosua.pharmacy.model;

import java.math.BigDecimal;
import java.util.UUID;
import com.akosua.pharmacy.exceptions.InsufficientStockException;
import com.akosua.pharmacy.exceptions.MissingPrescriberException;

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

    
        /** True when every dispense of this medication must record a prescriber. */
    public abstract boolean requiresPrescriber();

    /**
     * Checks the prescription rule. When {@link #requiresPrescriber()} is false the
     * prescriber argument is ignored, exactly as the original program did.
     * A medication with stricter rules can override this method.
     */
    public void validateDispensing(String prescriber) {
        if (requiresPrescriber() && (prescriber == null || prescriber.isBlank())) {
            throw new MissingPrescriberException(name);
        }
    }


    public final void dispenseStock(int quantity) {
        validatePositiveQuantity(quantity);
        if (quantity > stock) {
            throw new InsufficientStockException(name, quantity, stock);
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
