package com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions;

public class SpecificServiceException extends RuntimeException {
    public SpecificServiceException(String message) {
        super(message);
    }

    public SpecificServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
