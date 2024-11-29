package com.example.aos.sistema_aos_spring_boot.Controladores;
import com.example.aos.sistema_aos_spring_boot.Enums.Visibilidad;
import com.example.aos.sistema_aos_spring_boot.Modelo.Evento;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.SpecificServiceException;
import com.example.aos.sistema_aos_spring_boot.Servicios.EventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    private static final Logger logger = LoggerFactory.getLogger(EventoController.class);

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createEvento(
            @RequestParam("file") MultipartFile file,
            @RequestParam("nombre") String nombre,
            @RequestParam("fechaEvento") String fechaEventoStr,
            @RequestParam("visibilidad") String visibilidadStr,
            @RequestParam("fechaInicio") String fechaInicioStr,
            @RequestParam("fechaFin") String fechaFinStr,
            @RequestParam("cantidadQR") Integer cantidadQR,
            @RequestParam("codeIdentify") String codeIdentify) {

        Map<String, String> response = new HashMap<>();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

            LocalDateTime fechaEvento = LocalDateTime.parse(fechaEventoStr, formatter);
            LocalDateTime fechaInicio = LocalDateTime.parse(fechaInicioStr, formatter);
            LocalDateTime fechaFin = LocalDateTime.parse(fechaFinStr, formatter);

            Visibilidad visibilidad = Visibilidad.valueOf(visibilidadStr.toUpperCase());

            Evento evento = eventoService.saveEvento(file, nombre, fechaEvento, visibilidad, fechaInicio, fechaFin, cantidadQR,codeIdentify);
            response.put("message", "Evento creado exitosamente con ID: " + evento.getEventoId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DateTimeParseException e) {
            logger.warn("Error al parsear fechas: {}", e.getMessage());
            response.put("message", "Formato de fecha incorrecto.");
            return ResponseEntity.badRequest().body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Error con el argumento de visibilidad: {}", e.getMessage());
            response.put("message", "Visibilidad inválida.");
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            logger.error("Error de entrada/salida al crear el evento: {}", e.getMessage());
            response.put("message", "Fallo al crear el evento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            logger.error("Error inesperado al crear el evento: {}", e.getMessage());
            response.put("message", "Error inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }




    @PostMapping("/configuration")
    public ResponseEntity<Map<String, String>> createConfiguration(
            @RequestParam("fechaInicio") String fechaInicioStr,
            @RequestParam("fechaFin") String fechaFinStr,
            @RequestParam("visibilidad") String visibilidadStr,
            @RequestParam("activo") Boolean activo) {

        Map<String, String> response = new HashMap<>();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

            LocalDateTime fechaInicio = LocalDateTime.parse(fechaInicioStr, formatter);
            LocalDateTime fechaFin = LocalDateTime.parse(fechaFinStr, formatter);

            Visibilidad visibilidad = Visibilidad.valueOf(visibilidadStr.toUpperCase());

            Evento evento = eventoService.saveConfiguracion(fechaInicio, fechaFin, visibilidad, activo);
            response.put("message", "Configuración creada exitosamente con ID: " + evento.getEventoId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DateTimeParseException e) {
            logger.error("Error al parsear fechas: {}", e.getMessage());
            response.put("message", "Error al parsear fechas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Visibilidad no válida: {}", e.getMessage());
            response.put("message", "Visibilidad no válida: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (IOException e) {
            logger.error("Fallo al crear configuración: {}", e.getMessage());
            response.put("message", "Fallo al crear configuración: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            response.put("message", "Error inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    @GetMapping("/page/{page}")
    public ResponseEntity<Page<Evento>> listarEventos(@PathVariable("page") int page) {

        if (page < 0) {
            logger.warn("Se recibió un número de página negativo: {}", page);
            return ResponseEntity.badRequest().build();
        }

        try {
            Sort sort = Sort.by(Sort.Order.desc("eventoId"));
            PageRequest pageRequest = PageRequest.of(page, 10, sort);
            Page<Evento> eventosPage = eventoService.findAll(pageRequest);

            if (eventosPage.isEmpty() && page > 0) {
                logger.info("No se encontraron eventos en la página solicitada: {}", page);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok(eventosPage);
        } catch (Exception e) {
            logger.error("Error inesperado al listar eventos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @GetMapping("/activo")
    public ResponseEntity<List<Evento>> listarEventosActivos() {

        try {
            List<Evento> eventos = eventoService.obtenerEventoActivos();

            if (eventos == null) {
                logger.warn("La lista de eventos es nula. Retornando una lista vacía.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.emptyList());
            }

            eventos.sort(Comparator.comparing(Evento::getFechaEvento).reversed());

            return ResponseEntity.ok(eventos);
        } catch (SpecificServiceException e) {
            logger.error("Error al obtener eventos activos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.emptyList());
        } catch (Exception e) {
            logger.error("Error inesperado al listar eventos activos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }


    @PutMapping("/")
    public ResponseEntity<Evento> actualizarEvento(@RequestBody Evento evento) {

        if (evento == null || evento.getEventoId() == null) {
            logger.warn("El evento proporcionado es nulo o no tiene ID.");
            return ResponseEntity.badRequest().build();
        }

        try {
            Evento updatedEvento = eventoService.actualizarEvento(evento);
            return ResponseEntity.ok(updatedEvento);
        } catch (EntityNotFoundException e) {
            logger.warn("Evento no encontrado: {}", evento.getEventoId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            logger.warn("Argumento inválido para actualizar el evento: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar el evento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }




    @PutMapping("/{id}")
    public ResponseEntity<Evento> updateEvento(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam String fechaEvento,
            @RequestParam(required = false) MultipartFile imagen) {

        if (id == null) {
            logger.warn("El ID del evento no puede ser nulo.");
            return ResponseEntity.badRequest().build();
        }

        try {
            LocalDateTime fecha = LocalDateTime.parse(fechaEvento);
            Evento evento = eventoService.updateEvento(id, nombre, fecha, imagen);
            return ResponseEntity.ok(evento);
        } catch (DateTimeParseException e) {
            logger.warn("Formato de fecha inválido: {}", fechaEvento);
            return ResponseEntity.badRequest().body(null);
        } catch (EntityNotFoundException e) {
            logger.warn("Evento no encontrado para ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            logger.error("Error al procesar la imagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar el evento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/{eventoId}")
    public ResponseEntity<Evento> listarEvento(@PathVariable("eventoId") Long eventoId) {

        if (eventoId == null) {
            logger.warn("El ID del evento no puede ser nulo.");
            return ResponseEntity.badRequest().build();
        }

        try {
            Evento evento = eventoService.obtenerEvento(eventoId);
            if (evento == null) {
                logger.warn("Evento no encontrado para ID: {}", eventoId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(evento);
        } catch (Exception e) {
            logger.error("Error inesperado al listar el evento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{eventoId}")
    public ResponseEntity<Map<String, String>> eliminarEventos(@PathVariable("eventoId") Long eventoId) {
        Map<String, String> response = new HashMap<>();

        if (eventoId == null) {
            logger.warn("El ID del evento no puede ser nulo.");
            response.put("message", "El ID del evento no puede ser nulo.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            eventoService.eliminarEventos(eventoId);
            response.put("message", "Evento eliminado exitosamente.");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            logger.warn("No se puede eliminar el evento porque tiene registros asociados: {}", e.getMessage());
            response.put("message", "No se puede eliminar el evento porque tiene registros asociados.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (EntityNotFoundException e) {
            logger.warn("Evento no encontrado para ID: {}", eventoId);
            response.put("message", "Evento no encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar el evento: {}", e.getMessage());
            response.put("message", "Error al eliminar el evento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/")
    public ResponseEntity<Set<Evento>> listarEventos() {

        try {
            Set<Evento> eventos = eventoService.obtenerEventos();

            if (eventos == null || eventos.isEmpty()) {
                logger.warn("No se encontraron eventos.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            logger.error("Error inesperado al listar eventos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
