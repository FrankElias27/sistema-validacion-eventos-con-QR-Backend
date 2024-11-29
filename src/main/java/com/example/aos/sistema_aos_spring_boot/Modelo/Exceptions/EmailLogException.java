package com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions;

public class EmailLogException extends RuntimeException {
    public EmailLogException(String message) {
        super(message);
    }

    public EmailLogException(String message, Throwable cause) {
        super(message, cause);
    }
}
