package com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions;

public class EmailLimitExceededException extends RuntimeException {
    public EmailLimitExceededException(String message) {
        super(message);
    }
}
