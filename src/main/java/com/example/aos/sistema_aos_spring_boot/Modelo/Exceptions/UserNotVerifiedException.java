package com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions;

public class UserNotVerifiedException extends RuntimeException {
    public UserNotVerifiedException(String message) {
        super(message);
    }
}
