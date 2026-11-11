package com.akosua.pharmacy.exceptions;

public class MissingPrescriberException extends IllegalArgumentException {
    public MissingPrescriberException(String message) { super(message); }
}
