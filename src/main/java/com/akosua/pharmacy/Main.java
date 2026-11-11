package com.akosua.pharmacy;

import com.akosua.pharmacy.inventory.Inventory;
import com.akosua.pharmacy.ledger.Ledger;
import com.akosua.pharmacy.model.*;
import com.akosua.pharmacy.service.Pharmacy;
import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        Inventory inventory = new Inventory();
        Ledger ledger = new Ledger();
        Pharmacy pharmacy = new Pharmacy("Akosua's Pharmacy", inventory, ledger);

        Medication paracetamol = new OverTheCounterMedication(
                "Paracetamol 500mg", new BigDecimal("12.50"), 100, 20);
        Medication amoxicillin = new PrescriptionMedication(
                "Amoxicillin 250mg", new BigDecimal("30.00"), 40, 40);

        pharmacy.addMedication(paracetamol);
        pharmacy.addMedication(amoxicillin);

        Customer kofi = new WalkInCustomer("Kofi Mensah");
        Customer ama = new RegisteredPatient("Ama Owusu", "0244 000 000");
        pharmacy.addCustomer(kofi);
        pharmacy.addCustomer(ama);

        pharmacy.dispense(kofi, paracetamol, 2, null);
        pharmacy.dispense(ama, amoxicillin, 1, "Dr. Boateng");
        pharmacy.restock(paracetamol, 50);

        // This is deliberately rejected: only 39 Amoxicillin remain.
        try {
            pharmacy.dispense(kofi, amoxicillin, 100, "Dr. Boateng");
        } catch (RuntimeException exception) {
            System.out.println("Dispense rejected: " + exception.getMessage());
        }

        pharmacy.printReport();
        pharmacy.printLowStockAlerts();
    }
}
