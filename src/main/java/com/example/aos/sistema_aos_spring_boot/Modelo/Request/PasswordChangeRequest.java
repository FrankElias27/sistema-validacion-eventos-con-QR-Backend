package com.example.aos.sistema_aos_spring_boot.Modelo.Request;

public class PasswordChangeRequest {
    private String code;
    private String newPassword;

    // Getters y Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
