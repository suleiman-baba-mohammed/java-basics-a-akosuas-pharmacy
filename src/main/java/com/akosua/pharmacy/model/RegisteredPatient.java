package com.akosua.pharmacy.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class RegisteredPatient extends Customer {
    private final String phone;
    public RegisteredPatient(String name, String phone) {
        super(name);
        if (phone == null || phone.isBlank()) throw new IllegalArgumentException("Registered patient phone is required");
        this.phone = phone;
    }
    @Override public BigDecimal applyDiscount(BigDecimal subtotal) {
        return subtotal.multiply(new BigDecimal("0.95")).setScale(2, RoundingMode.HALF_UP);
    }
    @Override public String getKind() { return "REGISTERED"; }
    public String getPhone() { return phone; }
}
