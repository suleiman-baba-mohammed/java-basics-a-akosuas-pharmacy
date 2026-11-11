package com.akosua.pharmacy.model;

import java.math.BigDecimal;
import java.util.UUID;

public abstract class Customer {
    private final UUID id = UUID.randomUUID();
    private final String name;

    protected Customer(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Customer name is required");
        this.name = name;
    }
    public abstract BigDecimal applyDiscount(BigDecimal subtotal);
    public abstract String getKind();

    public final UUID getId() { return id; }
    
    public final String getName() { return name; }

    @Override
    public String toString() {
        return getKind() + " customer " + name;
    }
}

