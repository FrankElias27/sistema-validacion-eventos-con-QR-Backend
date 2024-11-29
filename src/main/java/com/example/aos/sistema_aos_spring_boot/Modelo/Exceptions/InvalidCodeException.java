package com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions;

public class InvalidCodeException extends RuntimeException {
    public InvalidCodeException(String message) {
        super(message);
    }
}
