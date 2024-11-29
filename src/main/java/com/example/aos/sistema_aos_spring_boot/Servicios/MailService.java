package com.example.aos.sistema_aos_spring_boot.Servicios;

import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.SpecificEmailException;

import java.util.Map;

public interface MailService {

    void sendVerificationEmail(String email) throws SpecificEmailException;
    void sendPasswordResetEmail(String email) throws SpecificEmailException;
}
