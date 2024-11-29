package com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}