package com.akosua.pharmacy.exceptions;

/** Thrown when more stock is requested than is available. */
public class InsufficientStockException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    private final String medicationName;
    private final int requested;
    private final int available;

    public InsufficientStockException(String medicationName, int requested, int available) {
        super("Insufficient stock for " + medicationName
                + ": requested " + requested + ", available " + available);
        this.medicationName = medicationName;
        this.requested = requested;
        this.available = available;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}