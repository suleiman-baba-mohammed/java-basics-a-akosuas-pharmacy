package com.akosua.pharmacy.model;

import java.math.BigDecimal;
import com.akosua.pharmacy.exceptions.MissingPrescriberException;

public class PrescriptionMedication extends Medication {
    public PrescriptionMedication(String name, BigDecimal price, int initialStock, int reorderLevel) {
        super(name, price, initialStock, reorderLevel);
    }
    @Override public void validateDispensing(String prescriber) {
        if (prescriber == null || prescriber.isBlank()) {
            throw new MissingPrescriberException("A valid prescriber is required for " + getName());
        }
    }
    @Override public String getCategory() { return "PRESCRIPTION"; }
}
