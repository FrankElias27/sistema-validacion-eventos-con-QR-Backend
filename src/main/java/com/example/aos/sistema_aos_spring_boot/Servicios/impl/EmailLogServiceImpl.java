package com.example.aos.sistema_aos_spring_boot.Servicios.impl;

import com.example.aos.sistema_aos_spring_boot.Modelo.EmailLog;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.LogsNoEncontradosException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Usuario;
import com.example.aos.sistema_aos_spring_boot.Repositorios.EmailLogRepository;
import com.example.aos.sistema_aos_spring_boot.Repositorios.UsuarioRepository;
import com.example.aos.sistema_aos_spring_boot.Servicios.EmailLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class EmailLogServiceImpl implements EmailLogService {

    @Autowired
    private EmailLogRepository emailLogRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public void deleteEmailLogsByUsuarioId(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<EmailLog> logs = emailLogRepository.findByUsuario(usuario);

        if (logs.isEmpty()) {
            throw new LogsNoEncontradosException("No se encontraron logs de email para este usuario.");
        }

        emailLogRepository.deleteByUsuario(usuario);
    }

    @Override
    public void eliminarRegistrosDeHoy(Usuario usuario) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDelDia = hoy.atStartOfDay();
        LocalDateTime finDelDia = hoy.atTime(23, 59, 59, 999999999);

        List<EmailLog> logsDeHoy = emailLogRepository.findByUsuarioAndSentAtBetween(usuario, inicioDelDia, finDelDia);

        if (logsDeHoy.isEmpty()) {
            throw new LogsNoEncontradosException("No se encontraron logs de email de hoy para este usuario.");
        }

        emailLogRepository.deleteAll(logsDeHoy);
    }

    @Override
    public boolean existenRegistrosDeHoy(Usuario usuario) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDelDia = hoy.atStartOfDay();
        LocalDateTime finDelDia = hoy.atTime(LocalTime.MAX);

        return emailLogRepository.countByUsuarioAndSentAtBetween(usuario, inicioDelDia, finDelDia) > 0;
    }
}
