package com.example.aos.sistema_aos_spring_boot.Modelo.Request;

public class QRCodeValidationResult {
    private boolean valid;
    private String message;

    public QRCodeValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }
}