package com.example.aos.sistema_aos_spring_boot.Servicios;

import com.example.aos.sistema_aos_spring_boot.Modelo.Evento;
import com.example.aos.sistema_aos_spring_boot.Modelo.RegistroEventos;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public interface RegistroService {


    RegistroEventos agregarRegistroEventos(RegistroEventos registroEventos);

    boolean verificarAsistencia(Long eventoId, Long usuarioId);


    RegistroEventos actualizarRegistro(RegistroEventos registroEventos);

    RegistroEventos obtenerRegistroEventoPorEventoYUsuario(Long eventoId, Long usuarioId);

    Page<RegistroEventos> getRegistroEventosByEventoNombre(Long eventoId, int page, int size);
}