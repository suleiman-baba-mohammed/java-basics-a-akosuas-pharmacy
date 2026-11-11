package com.akosua.pharmacy.exceptions;

/** Thrown when a medication that needs a prescriber is dispensed without one. */
public class MissingPrescriberException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    private final String medicationName;

    public MissingPrescriberException(String medicationName) {
        super("A valid prescriber is required for " + medicationName);
        this.medicationName = medicationName;
    }

    public String getMedicationName() {
        return medicationName;
    }
}
