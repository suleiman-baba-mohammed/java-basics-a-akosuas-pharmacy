package com.akosua.pharmacy.model;

import java.math.BigDecimal;

/** Prescription-only medicine. A prescriber must be recorded on every dispense. */
public class PrescriptionMedication extends Medication {
    public PrescriptionMedication(String name, BigDecimal price, int initialStock, int reorderLevel) {
        super(name, price, initialStock, reorderLevel);
    }
    @Override public boolean requiresPrescriber() { return true; }
    @Override public String getCategory() { return "PRESCRIPTION"; }
}
