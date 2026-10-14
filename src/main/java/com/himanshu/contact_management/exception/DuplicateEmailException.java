package com.himanshu.contact_management.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("A contact with email '" + email + "' already exists");
    }
}
