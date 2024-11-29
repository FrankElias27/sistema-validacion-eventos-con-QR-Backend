package com.example.aos.sistema_aos_spring_boot.Servicios;

import com.example.aos.sistema_aos_spring_boot.Enums.Visibilidad;
import com.example.aos.sistema_aos_spring_boot.Modelo.Evento;
import com.sun.media.sound.InvalidDataException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface EventoService {

    Evento saveEvento(MultipartFile file, String nombre, LocalDateTime fechaEvento, Visibilidad visibilidad,
                      LocalDateTime fechaInicio, LocalDateTime fechaFin, Integer cantidadQR, String codeIdentify)
            throws IOException, InvalidDataException;

    Evento saveConfiguracion(LocalDateTime fechaInicio, LocalDateTime fechaFin, Visibilidad visibilidad, boolean activo)
            throws IOException, InvalidDataException;

    Evento getEvento(Long id) ;

    Page<Evento> findAll(Pageable pageable);

    List<Evento> obtenerEventoActivos();

    Evento actualizarEvento(Evento evento) ;

    Evento updateEvento(Long id, String nombre, LocalDateTime fechaEvento, MultipartFile imagen) throws IOException;

    Evento obtenerEvento(Long eventoId);

    void actualizarEstadoEventos();

    void eliminarEventos(Long eventoId);

    Set<Evento> obtenerEventos();

    void updateEventoVisibilidad(Long eventoId, Visibilidad nuevaVisibilidad);
}
