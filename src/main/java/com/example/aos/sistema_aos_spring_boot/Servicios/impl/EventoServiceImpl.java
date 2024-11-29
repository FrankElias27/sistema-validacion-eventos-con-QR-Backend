package com.example.aos.sistema_aos_spring_boot.Servicios.impl;
import com.example.aos.sistema_aos_spring_boot.Enums.Visibilidad;
import com.example.aos.sistema_aos_spring_boot.Modelo.Evento;
import com.example.aos.sistema_aos_spring_boot.Repositorios.EventoRepository;
import com.example.aos.sistema_aos_spring_boot.Repositorios.RegistroRepository;
import com.example.aos.sistema_aos_spring_boot.Servicios.EventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.io.IOException;
import java.util.List;

import static sun.font.CreatedFontTracker.MAX_FILE_SIZE;

@Service
public class EventoServiceImpl implements EventoService {
    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private RegistroRepository registroEventosRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private void validateEventoInputs(MultipartFile file, String nombre, LocalDateTime fechaEvento) throws IOException {
        if (file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("El archivo es inválido.");
        }
        if (nombre == null || nombre.isEmpty()) {
            throw new IOException("El nombre del evento no puede estar vacío.");
        }
    }

    @Async
    @Override
    public Evento saveEvento(MultipartFile file, String nombre, LocalDateTime fechaEvento, Visibilidad visibilidad,
                             LocalDateTime fechaInicio, LocalDateTime fechaFin, Integer cantidadQR, String codeIdentify) throws IOException {
        validateEventoInputs(file, nombre, fechaEvento);

        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        ZoneId zonaBogota = ZoneId.of("America/Bogota");
        ZonedDateTime ahoraUTC = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime ahora = ahoraUTC.withZoneSameInstant(zonaBogota);


        if (ahora.toLocalDateTime().isBefore(fechaInicio)) {
            visibilidad = Visibilidad.PROXIMAMENTE;
        } else {
            visibilidad = Visibilidad.NORMAL;
        }

        Evento evento = new Evento();
        evento.setNombre(nombre);
        evento.setImagen(file.getBytes());
        evento.setFechaEvento(fechaEvento);
        evento.setVisibilidad(visibilidad);
        evento.setFechaInicio(fechaInicio);
        evento.setFechaFin(fechaFin);
        evento.setCantidadQR(cantidadQR);
        evento.setCodeIdentify(codeIdentify);

        return eventoRepository.save(evento);
    }

    @Override
    public Evento saveConfiguracion(LocalDateTime fechaInicio, LocalDateTime fechaFin, Visibilidad visibilidad, boolean activo) throws IOException {

        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        Evento evento = new Evento();
        evento.setFechaInicio(fechaInicio);
        evento.setFechaFin(fechaFin);
        evento.setVisibilidad(visibilidad);
        evento.setActivo(activo);

        return eventoRepository.save(evento);
    }

    @Override
    public Evento getEvento(Long id) {
        return eventoRepository.findById(id).orElse(null);
    }

    @Override
    public Page<Evento> findAll(Pageable pageable) {
        return eventoRepository.findAll(pageable);
    }

    @Cacheable("eventosActivos")
    @Override
    public List<Evento> obtenerEventoActivos() {
        return eventoRepository.findByActivo(true);
    }

    @Override
    public Evento actualizarEvento(Evento evento) {

        if (evento.getFechaFin().isBefore(evento.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        return eventoRepository.save(evento);
    }

    @Override
    public Evento updateEvento(Long id, String nombre, LocalDateTime fechaEvento, MultipartFile imagen) throws IOException {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new IOException("Evento no encontrado con ID: " + id));

        evento.setNombre(nombre);
        evento.setFechaEvento(fechaEvento);

        if (imagen != null && !imagen.isEmpty()) {
            evento.setImagen(imagen.getBytes());
        }

        return eventoRepository.save(evento);
    }

    @Override
    public Evento obtenerEvento(Long eventoId) {
        return eventoRepository.findById(eventoId).orElse(null);
    }

    @Override
    public void eliminarEventos(Long eventoId) {

        if (!eventoRepository.existsById(eventoId)) {
            throw new IllegalArgumentException("Evento no encontrado");
        }

        long registrosCount = registroEventosRepository.countByEventoId(eventoId);
        if (registrosCount > 0) {
            throw new IllegalStateException("No se puede eliminar el evento porque tiene registros asociados.");
        }

        eventoRepository.deleteById(eventoId);
    }

    @Override
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void actualizarEstadoEventos() {
        List<Evento> eventos = eventoRepository.findByActivo(true);
        ZoneId zonaBogota = ZoneId.of("America/Bogota");
        ZonedDateTime ahoraUTC = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime ahora = ahoraUTC.withZoneSameInstant(zonaBogota);

        List<Evento> eventosActualizados = new ArrayList<>();

        for (Evento evento : eventos) {
            LocalDateTime fechaInicio = evento.getFechaInicio();
            LocalDateTime fechaFin = evento.getFechaFin();

            ZonedDateTime inicioEvento = fechaInicio.atZone(zonaBogota);
            ZonedDateTime finEvento = fechaFin.atZone(zonaBogota);

            boolean enRango = ahora.isAfter(inicioEvento) && ahora.isBefore(finEvento);
            boolean antesDeInicio = ahora.isBefore(inicioEvento);
            boolean yaPasado = ahora.isAfter(finEvento);

            if (yaPasado) {
                evento.setActivo(false);
                evento.setVisibilidad(Visibilidad.SOLDOUT);
            } else if (enRango) {
                evento.setActivo(true);

                if (evento.getVisibilidad() == Visibilidad.SOLDOUT) {
                    evento.setVisibilidad(Visibilidad.SOLDOUT);
                } else {
                    evento.setVisibilidad(Visibilidad.NORMAL);
                }
            } else if (antesDeInicio) {
                evento.setActivo(true);
                evento.setVisibilidad(Visibilidad.PROXIMAMENTE);
            }

            eventosActualizados.add(evento);
        }

        eventoRepository.saveAll(eventosActualizados);
    }


    @Override
    public Set<Evento> obtenerEventos() {
        return new LinkedHashSet<>(eventoRepository.findAll());
    }

    @Override
    public void updateEventoVisibilidad(Long eventoId, Visibilidad nuevaVisibilidad) {

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        evento.setVisibilidad(nuevaVisibilidad);

        eventoRepository.save(evento);
    }

}
