package com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions;

public class LogsNoEncontradosException extends RuntimeException {
    public LogsNoEncontradosException(String message) {
        super(message);
    }
}
