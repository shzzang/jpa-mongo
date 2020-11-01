package com.omi.back.exception;

public class UserEmailExistException extends RuntimeException {
    public UserEmailExistException(String email) {
        super("Exist Email " + email);
    }
}
