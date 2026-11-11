package com.akosua.pharmacy.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Customer {
    private final UUID id = UUID.randomUUID();
    private final String name;
    private final List<String> history = new ArrayList<>();

    protected Customer(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Customer name is required");
        this.name = name;
    }
    public abstract BigDecimal applyDiscount(BigDecimal subtotal);
    public abstract String getKind();
    public final void recordDispense(String description) { history.add(description); }
    public final List<String> getHistory() { return List.copyOf(history); }
    public final UUID getId() { return id; }
    public final String getName() { return name; }
}
