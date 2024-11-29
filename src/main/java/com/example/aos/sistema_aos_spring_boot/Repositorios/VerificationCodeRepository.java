package com.example.aos.sistema_aos_spring_boot.Repositorios;

import com.example.aos.sistema_aos_spring_boot.Modelo.Usuario;
import com.example.aos.sistema_aos_spring_boot.Modelo.VerificationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

import java.util.List;

public interface VerificationCodeRepository extends JpaRepository<VerificationCodeEntity,Long> {
    VerificationCodeEntity findByUsuarioEmail(String email);
    VerificationCodeEntity findByCode(String code);
    void deleteByUsuarioId(Long usuarioId);

    @Query("SELECT v FROM VerificationCodeEntity v WHERE DATE(v.expirationTime) = DATE(:today) AND v.usuario.id = :usuarioId")
    List<VerificationCodeEntity> findByExpirationDateAndUsuarioId(@Param("today") LocalDateTime today, @Param("usuarioId") Long usuarioId);
}
