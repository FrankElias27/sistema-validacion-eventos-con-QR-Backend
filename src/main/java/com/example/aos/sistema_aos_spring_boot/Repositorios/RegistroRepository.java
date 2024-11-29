package com.example.aos.sistema_aos_spring_boot.Repositorios;

import com.example.aos.sistema_aos_spring_boot.Enums.Asistencia;
import com.example.aos.sistema_aos_spring_boot.Enums.Registro;
import com.example.aos.sistema_aos_spring_boot.Modelo.RegistroEventos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistroRepository extends JpaRepository<RegistroEventos,Long> {

    boolean existsByEvento_EventoIdAndUsuario_IdAndEstadoRegistro(Long eventoId, Long usuarioId, Registro estadoRegistro);

    int countByEvento_EventoId(Long eventoId);

    RegistroEventos findByEvento_EventoIdAndUsuario_Id(Long eventoId, Long usuarioId);

    Page<RegistroEventos> findByEvento_EventoId_AndEstadoAsistencia(Long eventoId, Asistencia estadoAsistencia, Pageable pageable);

    @Query("SELECT COUNT(r) FROM RegistroEventos r WHERE r.evento.eventoId = :eventoId")
    long countByEventoId(@Param("eventoId") Long eventoId);

    long countByUsuarioId(Long usuarioId);
}
