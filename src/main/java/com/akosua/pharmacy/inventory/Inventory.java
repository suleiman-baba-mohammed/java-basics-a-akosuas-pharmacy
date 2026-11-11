package com.akosua.pharmacy.inventory;

import com.akosua.pharmacy.model.Medication;
import java.util.*;

public final class Inventory {
    private final Map<UUID, Medication> medications = new LinkedHashMap<>();

    public void add(Medication medication) {
        Objects.requireNonNull(medication, "Medication is required");
        boolean duplicateName = medications.values().stream()
                .anyMatch(existing -> existing.getName().equalsIgnoreCase(medication.getName()));
        if (duplicateName) throw new IllegalArgumentException("Medication already exists: " + medication.getName());
        medications.put(medication.getId(), medication);
    }

    public List<Medication> getAll() { return List.copyOf(medications.values()); }

    public List<Medication> getLowStockMedications() {
        return medications.values().stream().filter(Medication::isLowStock).toList();
    }

    public boolean contains(Medication medication) {
        return medication != null && medications.containsKey(medication.getId());
    }
}
