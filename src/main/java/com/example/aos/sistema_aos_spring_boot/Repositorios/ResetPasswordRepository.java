package com.example.aos.sistema_aos_spring_boot.Repositorios;

import com.example.aos.sistema_aos_spring_boot.Modelo.ResetPasswordEntity;
import com.example.aos.sistema_aos_spring_boot.Modelo.VerificationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResetPasswordRepository extends JpaRepository<ResetPasswordEntity,Long> {
    ResetPasswordEntity findByCode(String code);
}
