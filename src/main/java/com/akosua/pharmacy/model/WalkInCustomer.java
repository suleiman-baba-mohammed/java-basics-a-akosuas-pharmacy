package com.akosua.pharmacy.model;

import java.math.BigDecimal;

public final class WalkInCustomer extends Customer {
    public WalkInCustomer(String name) { super(name); }
    @Override public BigDecimal applyDiscount(BigDecimal subtotal) { return subtotal; }
    @Override public String getKind() { return "WALK_IN"; }
}
