package com.example.aos.sistema_aos_spring_boot.Controladores;


import com.example.aos.sistema_aos_spring_boot.Enums.Asistencia;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.SpecificServiceException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Request.VerificarAsistenciaRequest;
import com.example.aos.sistema_aos_spring_boot.Modelo.RegistroEventos;
import com.example.aos.sistema_aos_spring_boot.Repositorios.RegistroRepository;
import com.example.aos.sistema_aos_spring_boot.Servicios.RegistroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/registroEvento")
public class RegistroController {

    @Autowired
    private RegistroService registroService;

    @Autowired
    private RegistroRepository registroRepository;

    private static final Logger logger = LoggerFactory.getLogger(RegistroController.class);

    @PostMapping("/")
    public ResponseEntity<RegistroEventos> guardarRegistroEvento(@RequestBody RegistroEventos registroEventos) {

        try {
            RegistroEventos nuevoRegistro = registroService.agregarRegistroEventos(registroEventos);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoRegistro);
        } catch (IllegalArgumentException e) {
            logger.warn("Argumento inválido al guardar el registro de evento: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (SpecificServiceException e) {
            logger.error("Error al guardar el registro de evento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            logger.error("Error inesperado al guardar el registro de evento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/verificar")
    public ResponseEntity<Boolean> verificarAsistencia(@RequestBody VerificarAsistenciaRequest request) {

        try {
            boolean asistencia = registroService.verificarAsistencia(request.getEventoId(), request.getUsuarioId());
            return ResponseEntity.ok(asistencia);
        } catch (SpecificServiceException e) {
            logger.error("Error al verificar asistencia: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        } catch (NullPointerException e) {
            logger.warn("Se recibió un request nulo o con valores inválidos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        } catch (Exception e) {
            logger.error("Error inesperado al verificar asistencia: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PutMapping("/")
    public ResponseEntity<RegistroEventos> actualizarRegistro(@RequestBody RegistroEventos registroEventos) {

        try {
            RegistroEventos actualizado = registroService.actualizarRegistro(registroEventos);
            return ResponseEntity.ok(actualizado);
        } catch (EntityNotFoundException e) {
            logger.error("Registro no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException e) {
            logger.error("Error en los argumentos proporcionados: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar el registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{eventoId}/{usuarioId}")
    public ResponseEntity<Long> obtenerRegistroEventoId(
            @PathVariable("eventoId") Long eventoId,
            @PathVariable("usuarioId") Long usuarioId) {

        try {
            RegistroEventos registroEvento = registroService.obtenerRegistroEventoPorEventoYUsuario(eventoId, usuarioId);
            return ResponseEntity.ok(registroEvento.getRegistroEventoId());
        } catch (EntityNotFoundException e) {
            logger.error("Registro de evento no encontrado para eventoId: {} y usuarioId: {}", eventoId, usuarioId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error inesperado al obtener el registro de evento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/update-asistencia")
    public ResponseEntity<Map<String, Object>> actualizarRegistroEvento(@RequestBody Map<String, Object> updates) {
        Long registroEventoId;

        try {
            registroEventoId = Long.parseLong(updates.get("id").toString());
        } catch (NumberFormatException e) {
            logger.error("Error al parsear el ID de registro: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "ID de registro inválido"));
        }

        try {
            RegistroEventos registroEvento = registroRepository.findById(registroEventoId)
                    .orElseThrow(() -> new EntityNotFoundException("Registro de evento con ID " + registroEventoId + " no encontrado"));


            String portero = (String) updates.get("portero");
            if (portero == null || portero.isEmpty()) {
                logger.warn("El campo 'portero' es obligatorio pero no fue proporcionado.");
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "El campo 'portero' es obligatorio"));
            }

            Asistencia estadoAsistencia;
            if (updates.containsKey("estadoAsistencia")) {
                try {
                    estadoAsistencia = Asistencia.valueOf(updates.get("estadoAsistencia").toString());
                } catch (IllegalArgumentException e) {
                    logger.error("Estado de asistencia inválido: {}", updates.get("estadoAsistencia"));
                    return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Estado de asistencia inválido"));
                }
            } else {
                logger.warn("El campo 'estadoAsistencia' es obligatorio pero no fue proporcionado.");
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "El campo 'estadoAsistencia' es obligatorio"));
            }

            registroEvento.setPortero(portero);
            registroEvento.setEstadoAsistencia(estadoAsistencia);
            registroEvento.setFechaRegistro(LocalDateTime.now());

            RegistroEventos registroEventoActualizado = registroRepository.save(registroEvento);
            return ResponseEntity.ok(Collections.singletonMap("registroActualizado", registroEventoActualizado));

        } catch (EntityNotFoundException e) {
            logger.error("No se encontró el registro de evento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error interno del servidor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", "Error interno del servidor"));
        }
    }


    @GetMapping("/historial-asistencia")
    public ResponseEntity<Page<RegistroEventos>> getRegistroEventosByEventoNombre(
            @RequestParam Long eventoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size <= 0) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            Page<RegistroEventos> registros = registroService.getRegistroEventosByEventoNombre(eventoId, page, size);
            if (registros.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(registros);
        } catch (EntityNotFoundException e) {
            logger.error("No se encontraron registros para el eventoId: {}", eventoId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error inesperado al obtener el historial de asistencia: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
