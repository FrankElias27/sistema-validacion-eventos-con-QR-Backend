package com.example.aos.sistema_aos_spring_boot.Repositorios;

import com.example.aos.sistema_aos_spring_boot.Modelo.EmailLog;
import com.example.aos.sistema_aos_spring_boot.Modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    long countByUsuarioAndEmailTypeAndSentAtAfter(Usuario usuario, String emailType, LocalDateTime sentAt);
    List<EmailLog> findByUsuarioAndSentAtBetween(Usuario usuario, LocalDateTime startDate, LocalDateTime endDate);
    List<EmailLog> findByUsuario(Usuario usuario);
    long countByUsuarioAndSentAtBetween(Usuario usuario, LocalDateTime inicio, LocalDateTime fin);
    long countByUsuarioId(Long usuarioId);
    void deleteByUsuario(Usuario usuario);


}
