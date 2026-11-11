package com.akosua.pharmacy.model;

import java.math.BigDecimal;

public final class RegisteredPatient extends Customer {
      /** Share taken off the price for registered patients (5%). */
    private static final BigDecimal REGISTERED_DISCOUNT_RATE = new BigDecimal("0.05");

    private final String phone;

    public RegisteredPatient(String name, String phone) {
        super(name);
        if (phone == null || phone.isBlank()) throw new IllegalArgumentException("Registered patient phone is required");
        this.phone = phone;
    }

    @Override public BigDecimal applyDiscount(BigDecimal subtotal) {
        return subtotal.multiply(BigDecimal.ONE.subtract(REGISTERED_DISCOUNT_RATE));
    }


    @Override public String getKind() { return "REGISTERED"; }
    public String getPhone() { return phone; }
}
