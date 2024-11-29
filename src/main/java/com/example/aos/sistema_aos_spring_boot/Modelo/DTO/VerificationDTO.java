package com.example.aos.sistema_aos_spring_boot.Modelo.DTO;

public class VerificationDTO {
    private String email;
    private String verificationCode;

    public VerificationDTO() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}
