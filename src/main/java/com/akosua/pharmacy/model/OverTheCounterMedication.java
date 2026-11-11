package com.akosua.pharmacy.model;

import java.math.BigDecimal;

/** Medicine anyone can buy. No prescriber is needed. */
public final class OverTheCounterMedication extends Medication {
    public OverTheCounterMedication(String name, BigDecimal price, int initialStock, int reorderLevel) {
        super(name, price, initialStock, reorderLevel);
    }
    @Override public boolean requiresPrescriber() { return false; }
    @Override public String getCategory() { return "OTC"; }
}
