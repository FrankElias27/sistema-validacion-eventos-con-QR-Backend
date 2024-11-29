package com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions;

public class AccountBannedException extends RuntimeException{
    public AccountBannedException(String message) {
        super(message);
    }
}
