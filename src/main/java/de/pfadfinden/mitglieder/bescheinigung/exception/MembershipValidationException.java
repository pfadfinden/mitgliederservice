package de.pfadfinden.mitglieder.bescheinigung.exception;

public class MembershipValidationException extends Exception {

    public MembershipValidationException() {}

    public MembershipValidationException(String message)
    {
        super(message);
    }
}