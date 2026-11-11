package com.akosua.pharmacy.model;

import java.math.BigDecimal;

public final class OverTheCounterMedication extends Medication {
    public OverTheCounterMedication(String name, BigDecimal price, int initialStock, int reorderLevel) {
        super(name, price, initialStock, reorderLevel);
    }
    @Override public void validateDispensing(String prescriber) { /* OTC medicine needs no prescriber. */ }
    @Override public String getCategory() { return "OTC"; }
}
