package com.akosua.pharmacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.akosua.pharmacy.exceptions.InsufficientStockException;
import com.akosua.pharmacy.exceptions.MissingPrescriberException;
import com.akosua.pharmacy.inventory.Inventory;
import com.akosua.pharmacy.ledger.Ledger;
import com.akosua.pharmacy.model.Customer;
import com.akosua.pharmacy.model.Medication;
import com.akosua.pharmacy.model.OverTheCounterMedication;
import com.akosua.pharmacy.model.PrescriptionMedication;
import com.akosua.pharmacy.model.RegisteredPatient;
import com.akosua.pharmacy.model.Transaction;
import com.akosua.pharmacy.model.TransactionType;
import com.akosua.pharmacy.model.WalkInCustomer;
import com.akosua.pharmacy.service.Pharmacy;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Each test builds its own pharmacy in {@link #setUp()}, so tests do not depend on
 * each other or on the order they run in.
 */
public class PharmacyTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream printed;

    private Pharmacy pharmacy;
    private Medication paracetamol;
    private Medication amoxicillin;
    private Customer kofi;
    private Customer ama;

    @Before
    public void setUp() {
        printed = new ByteArrayOutputStream();
        System.setOut(new PrintStream(printed, true, StandardCharsets.UTF_8));

        pharmacy = new Pharmacy("Akosua's Pharmacy", new Inventory(), new Ledger());
        paracetamol = new OverTheCounterMedication("Paracetamol 500mg", new BigDecimal("12.50"), 100, 20);
        amoxicillin = new PrescriptionMedication("Amoxicillin 250mg", new BigDecimal("30.00"), 40, 40);
        kofi = new WalkInCustomer("Kofi Mensah");
        ama = new RegisteredPatient("Ama Owusu", "0244 000 000");
        pharmacy.addMedication(paracetamol);
        pharmacy.addMedication(amoxicillin);
        pharmacy.addCustomer(kofi);
        pharmacy.addCustomer(ama);
    }

    @After
    public void restoreOutput() {
        System.setOut(originalOut);
    }

    private List<String> printedLines() {
        return printed.toString(StandardCharsets.UTF_8).lines().toList();
    }

    // ---- behaviour carried over from the original program ----

    @Test
    public void dispensingReducesStockAndPrintsTheOriginalLine() {
        pharmacy.dispense(kofi, paracetamol, 2, null);

        assertEquals(98, paracetamol.getStock());
        assertEquals("Dispensed 2 x Paracetamol 500mg to Kofi Mensah for GHS 25.00", printedLines().get(0));
    }

    @Test
    public void prescriptionLinesNameThePrescriber() {
        pharmacy.dispense(ama, amoxicillin, 1, "Dr. Boateng");

        assertEquals("Dispensed 1 x Amoxicillin 250mg to Ama Owusu (prescribed by Dr. Boateng) for GHS 28.50",
                printedLines().get(0));
    }

    @Test
    public void registeredPatientsGetFivePercentOffAndWalkInsPayFullPrice() {
        pharmacy.dispense(kofi, paracetamol, 2, null);
        pharmacy.dispense(ama, paracetamol, 2, null);

        List<Transaction> transactions = pharmacy.getTransactions();
        assertEquals(new BigDecimal("25.00"), transactions.get(0).total());
        assertEquals(new BigDecimal("23.75"), transactions.get(1).total());
    }

    @Test
    public void totalsAreRoundedOnceToTwoDecimalPlaces() {
        // 12.50 x 0.95 = 11.875, which rounds half-up to 11.88 for every customer type
        pharmacy.dispense(ama, paracetamol, 1, null);

        assertEquals(new BigDecimal("11.88"), pharmacy.getTransactions().get(0).total());
    }

    @Test
    public void restockAddsStockAndRecordsASupplierTransaction() {
        pharmacy.restock(paracetamol, 50);

        assertEquals(150, paracetamol.getStock());
        Transaction restock = pharmacy.getTransactions().get(0);
        assertEquals(TransactionType.RESTOCK, restock.type());
        assertEquals(Optional.empty(), restock.customerId());
        assertEquals("Restocked 50 x Paracetamol 500mg", printedLines().get(0));
    }

    @Test
    public void overTheCounterIgnoresAPrescriberAsTheOriginalDid() {
        pharmacy.dispense(kofi, paracetamol, 1, "Dr. Boateng");

        assertEquals(Optional.empty(), pharmacy.getTransactions().get(0).prescriber());
        assertFalse(printedLines().get(0).contains("prescribed by"));
    }

    @Test
    public void reportListsStockAndTransactionCount() {
        pharmacy.dispense(kofi, paracetamol, 2, null);
        printed.reset();

        pharmacy.printReport();

        List<String> lines = printedLines();
        assertTrue(lines.contains("=== Akosua's Pharmacy - end of day ==="));
        assertTrue(lines.contains("  Paracetamol 500mg (OTC): 98 in stock"));
        assertTrue(lines.contains("  Amoxicillin 250mg (PRESCRIPTION): 40 in stock"));
        assertTrue(lines.contains("Transactions today: 1"));
    }

    // ---- the rule that must never be broken ----

    @Test
    public void dispensingMoreThanInStockIsRefusedAndChangesNothing() {
        InsufficientStockException refusal = assertThrows(InsufficientStockException.class,
                () -> pharmacy.dispense(kofi, amoxicillin, 41, "Dr. Boateng"));

        assertEquals("Amoxicillin 250mg", refusal.getMedicationName());
        assertEquals(41, refusal.getRequested());
        assertEquals(40, refusal.getAvailable());
        assertEquals(40, amoxicillin.getStock());
        assertTrue(pharmacy.getTransactions().isEmpty());
        assertTrue(pharmacy.getHistory(kofi).isEmpty());
    }

    @Test
    public void dispensingExactlyWhatIsLeftReachesZeroNotBelow() {
        pharmacy.dispense(kofi, amoxicillin, 40, "Dr. Boateng");

        assertEquals(0, amoxicillin.getStock());
        assertThrows(InsufficientStockException.class,
                () -> pharmacy.dispense(kofi, amoxicillin, 1, "Dr. Boateng"));
        assertEquals(0, amoxicillin.getStock());
    }

    @Test
    public void zeroAndNegativeQuantitiesAreRejected() {
        for (int badQuantity : new int[] {0, -1, -100}) {
            assertThrows(IllegalArgumentException.class,
                    () -> pharmacy.dispense(kofi, paracetamol, badQuantity, null));
            assertThrows(IllegalArgumentException.class,
                    () -> pharmacy.restock(paracetamol, badQuantity));
        }
        assertEquals(100, paracetamol.getStock());
        assertTrue(pharmacy.getTransactions().isEmpty());
    }

    @Test
    public void invalidMedicationsAndCustomersCannotBeCreated() {
        BigDecimal price = new BigDecimal("1.00");
        assertThrows(IllegalArgumentException.class, () -> new OverTheCounterMedication("X", price, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new OverTheCounterMedication(" ", price, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new OverTheCounterMedication("X", BigDecimal.ZERO, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new OverTheCounterMedication("X", price, 1, -1));
        assertThrows(IllegalArgumentException.class, () -> new WalkInCustomer(""));
        assertThrows(IllegalArgumentException.class, () -> new RegisteredPatient("Ama", " "));
    }

    // ---- prescriptions ----

    @Test
    public void prescriptionMedicationWithoutAPrescriberIsRefused() {
        for (String missing : new String[] {null, "", "   "}) {
            MissingPrescriberException refusal = assertThrows(MissingPrescriberException.class,
                    () -> pharmacy.dispense(ama, amoxicillin, 1, missing));
            assertEquals("Amoxicillin 250mg", refusal.getMedicationName());
        }
        assertEquals(40, amoxicillin.getStock());
        assertTrue(pharmacy.getTransactions().isEmpty());
    }

    @Test
    public void thePrescriberIsStoredOnTheTransaction() {
        pharmacy.dispense(ama, amoxicillin, 1, "Dr. Boateng");

        assertEquals(Optional.of("Dr. Boateng"), pharmacy.getTransactions().get(0).prescriber());
    }

    // ---- single source of truth and protected data ----

    @Test
    public void customerHistoryIsReadFromTheLedger() {
        pharmacy.dispense(kofi, paracetamol, 2, null);
        pharmacy.dispense(ama, amoxicillin, 1, "Dr. Boateng");
        pharmacy.restock(paracetamol, 10);

        assertEquals(List.of("Got 2 x Paracetamol 500mg"), pharmacy.getHistory(kofi));
        assertEquals(List.of("Got 1 x Amoxicillin 250mg"), pharmacy.getHistory(ama));
    }

    @Test
    public void ledgerAndInventoryHandOutReadOnlyCopies() {
        pharmacy.dispense(kofi, paracetamol, 1, null);

        assertThrows(UnsupportedOperationException.class, () -> pharmacy.getTransactions().clear());
        assertEquals(1, pharmacy.getTransactions().size());
    }

    @Test
    public void transactionsHoldIdsNotLiveObjects() {
        pharmacy.dispense(kofi, paracetamol, 1, null);

        Transaction transaction = pharmacy.getTransactions().get(0);
        assertEquals(Optional.of(kofi.getId()), transaction.customerId());
        assertEquals(paracetamol.getId(), transaction.medicationId());
        assertEquals("Paracetamol 500mg", transaction.medicationName());
    }

    @Test
    public void transactionsRefuseInconsistentData() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        UUID medicationId = paracetamol.getId();
        Optional<UUID> customer = Optional.of(kofi.getId());
        BigDecimal ten = BigDecimal.TEN;

        // a dispense with a zero total, or with no customer
        assertThrows(IllegalArgumentException.class, () -> new Transaction(id, now, TransactionType.DISPENSE,
                customer, medicationId, "X", 1, BigDecimal.ZERO, Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> new Transaction(id, now, TransactionType.DISPENSE,
                Optional.empty(), medicationId, "X", 1, ten, Optional.empty()));
        // a restock that names a customer or carries a price
        assertThrows(IllegalArgumentException.class, () -> new Transaction(id, now, TransactionType.RESTOCK,
                customer, medicationId, "X", 1, BigDecimal.ZERO, Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> new Transaction(id, now, TransactionType.RESTOCK,
                Optional.empty(), medicationId, "X", 1, ten, Optional.empty()));
    }

    @Test
    public void aMissingTransactionFieldIsNamedInTheError() {
        NullPointerException problem = assertThrows(NullPointerException.class,
                () -> new Transaction(UUID.randomUUID(), Instant.now(), TransactionType.RESTOCK,
                        Optional.empty(), null, "X", 1, BigDecimal.ZERO, Optional.empty()));

        assertTrue(problem.getMessage().contains("medicationId"));
    }

    @Test
    public void unknownCustomersAndMedicationsAreRefused() {
        Medication stranger = new OverTheCounterMedication("Stranger", BigDecimal.ONE, 1, 0);
        Customer nobody = new WalkInCustomer("Nobody");

        assertThrows(IllegalArgumentException.class, () -> pharmacy.dispense(kofi, stranger, 1, null));
        assertThrows(IllegalArgumentException.class, () -> pharmacy.dispense(nobody, paracetamol, 1, null));
        assertThrows(IllegalArgumentException.class, () -> pharmacy.restock(stranger, 1));
    }

    @Test
    public void duplicateNamesAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> pharmacy.addMedication(
                new OverTheCounterMedication("paracetamol 500MG", BigDecimal.ONE, 1, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> pharmacy.addCustomer(new WalkInCustomer("kofi mensah")));
    }

    @Test
    public void aPatientsPhoneNumberIsMaskedWhenPrinted() {
        String text = ama.toString();

        assertFalse(text.contains("0244"));
        assertTrue(text.endsWith("(phone *******000)"));
    }

    // ---- low stock ----

    @Test
    public void lowStockAlertsListMedicationsAtOrBelowTheirReorderLevel() {
        pharmacy.dispense(kofi, amoxicillin, 1, "Dr. Boateng"); // 39 left, reorder level 40
        printed.reset();

        pharmacy.printLowStockAlerts();

        assertTrue(printedLines().contains(
                "LOW STOCK: Amoxicillin 250mg - 39 remaining (reorder level: 40)"));
        assertFalse(printedLines().toString().contains("Paracetamol"));
    }

    // ---- the change that is coming ----

    /** A new category defined only in this test. No class in src/main is edited for it. */
    private static final class ControlledMedication extends Medication {
        ControlledMedication(String name, BigDecimal price, int stock) {
            super(name, price, stock, 5);
        }

        @Override
        public String getCategory() {
            return "CONTROLLED";
        }

        @Override
        public boolean requiresPrescriber() {
            return true;
        }
    }

    @Test
    public void aControlledSubstanceNeedsOnlyANewClass() {
        Medication morphine = new ControlledMedication("Morphine 10mg", new BigDecimal("80.00"), 10);
        pharmacy.addMedication(morphine);

        assertThrows(MissingPrescriberException.class, () -> pharmacy.dispense(ama, morphine, 1, null));
        pharmacy.dispense(ama, morphine, 2, "Dr. Boateng");

        Transaction transaction = pharmacy.getTransactions().get(0);
        assertEquals(Optional.of("Dr. Boateng"), transaction.prescriber());
        assertEquals(new BigDecimal("152.00"), transaction.total());
        assertEquals(8, morphine.getStock());
        assertThrows(InsufficientStockException.class,
                () -> pharmacy.dispense(ama, morphine, 9, "Dr. Boateng"));
        assertEquals(8, morphine.getStock());
    }

    // ---- the demo program end to end ----

    @Test
    public void theDemoProgramRunsTheOriginalMorningAndRefusesTheOverDispense() {
        printed.reset();

        Main.main(new String[0]);

        List<String> lines = printedLines();
        assertEquals("Dispensed 2 x Paracetamol 500mg to Kofi Mensah for GHS 25.00", lines.get(0));
        assertEquals("Dispensed 1 x Amoxicillin 250mg to Ama Owusu (prescribed by Dr. Boateng) for GHS 28.50",
                lines.get(1));
        assertEquals("Restocked 50 x Paracetamol 500mg", lines.get(2));
        assertEquals("Dispense rejected: Insufficient stock for Amoxicillin 250mg: requested 100, available 39",
                lines.get(3));
        assertTrue(lines.contains("  Paracetamol 500mg (OTC): 148 in stock"));
        assertTrue(lines.contains("  Amoxicillin 250mg (PRESCRIPTION): 39 in stock"));
        assertTrue(lines.contains("Transactions today: 3"));
    }
}
