package com.example.aos.sistema_aos_spring_boot.Controladores;


import com.example.aos.sistema_aos_spring_boot.Enums.Asistencia;
import com.example.aos.sistema_aos_spring_boot.Enums.TipoReporteEnum;
import com.example.aos.sistema_aos_spring_boot.Modelo.*;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.SpecificServiceException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Request.QRCodeConfirmedRequest;
import com.example.aos.sistema_aos_spring_boot.Modelo.Request.QRCodeValidationResult;
import com.example.aos.sistema_aos_spring_boot.Modelo.Request.ValorQRRequest;
import com.example.aos.sistema_aos_spring_boot.Repositorios.RegistroRepository;
import com.example.aos.sistema_aos_spring_boot.Servicios.*;
import com.google.zxing.WriterException;
import net.sf.jasperreports.engine.JRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/porteros")
public class PorteroController {

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private RegistroService registroService;

    @Autowired
    private EventoService eventoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RegistroRepository registroRepository;

    private static final Logger logger = LoggerFactory.getLogger(PorteroController.class);

    @PostMapping("/generate/confirmed")
    public CompletableFuture<ResponseEntity<String>> generateQRCodeConfirmed(@RequestBody QRCodeConfirmedRequest qrCodeRequest) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                String event = qrCodeRequest.getEvent();
                String user = qrCodeRequest.getUser();
                String dni = qrCodeRequest.getDni();

                String qrCodeText = "EventID:" + event + ";UserID:" + user + ";Dni:" + dni;

                String qrCodeImage = qrCodeService.generateQRCodeImage(qrCodeText).join();
                return ResponseEntity.ok(qrCodeImage);
            } catch (WriterException e) {
                logger.error("Error al generar el código QR: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al generar el código QR: " + e.getMessage());
            } catch (IOException e) {
                logger.error("Error de entrada/salida: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error de entrada/salida: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Error inesperado al generar el código QR: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error inesperado: " + e.getMessage());
            }
        });
    }


    @PostMapping("/validate")
    public ResponseEntity<Map<String, String>> validateQRCode(@RequestBody QRCode qrCodeRequest) {
        Map<String, String> response = new HashMap<>();

        if (qrCodeRequest.getValorQR() == null || qrCodeRequest.getValorQR().isEmpty()) {
            response.put("message", "El código QR no puede estar vacío.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            QRCodeValidationResult validationResult = qrCodeService.validateQRCode(qrCodeRequest.getValorQR());
            response.put("message", validationResult.getMessage());

            if (validationResult.isValid()) {
                return ResponseEntity.ok(response);
            } else if ("QR Code already used.".equals(validationResult.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (SpecificServiceException e) {
            logger.error("Error al validar el código QR: {}", e.getMessage());
            response.put("message", "Error en el servicio de validación del código QR.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            logger.error("Error inesperado al validar el código QR: {}", e.getMessage());
            response.put("message", "Error inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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

    @PostMapping("/validar")
    public CompletableFuture<ResponseEntity<Boolean>> verificarQRCode(@RequestBody ValorQRRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean usado = qrCodeService.isQRCodeUsed(request.getValorQR());
                return ResponseEntity.ok(usado);
            } catch (IllegalArgumentException e) {
                logger.error("Error en los datos de entrada: {}", e.getMessage());
                return ResponseEntity.badRequest().body(false);
            } catch (Exception e) {
                logger.error("Error inesperado al verificar el código QR: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(false);
            }
        });
    }


    @GetMapping(path = "/asistencia")
    public ResponseEntity<Resource> downloadAsistencia(@RequestParam Map<String, Object> params)
            throws JRException, IOException, SQLException {
        logger.info("Descargando reporte de asistencia con parámetros: {}", params);
        return downloadReporte(reporteService.obtenerReporteAsistencia(params), params);
    }

    @GetMapping(path = "/invitados")
    public ResponseEntity<Resource> downloadEstado(@RequestParam Map<String, Object> params)
            throws JRException, IOException, SQLException {
        logger.info("Descargando reporte de estado con parámetros: {}", params);
        return downloadReporte(reporteService.obtenerReporteEstado(params), params);
    }

    @GetMapping(path = "/invitadoPorUsuario")
    public ResponseEntity<Resource> downloadInvitados(@RequestParam Map<String, Object> params)
            throws JRException, IOException, SQLException {
        logger.info("Descargando reporte de invitados con parámetros: {}", params);
        return downloadReporte(reporteService.obtenerReporteInvitados(params), params);
    }

    private ResponseEntity<Resource> downloadReporte(Reporte dto, Map<String, Object> params) {

        if (dto == null || dto.getStream() == null) {
            logger.error("El reporte está vacío o el stream es nulo.");
            return ResponseEntity.badRequest().body(null);
        }

        MediaType mediaType;
        try {
            String tipo = params.get("tipo").toString();
            mediaType = tipo.equalsIgnoreCase(TipoReporteEnum.EXCEL.name())
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.APPLICATION_PDF;
        } catch (Exception e) {
            logger.error("Error al determinar el tipo de reporte: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }

        InputStreamResource streamResource;
        try {
            streamResource = new InputStreamResource(dto.getStream());
        } catch (Exception e) {
            logger.error("Error al crear el InputStreamResource: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"" + dto.getFileName() + "\"")
                .contentLength(dto.getLength())
                .contentType(mediaType)
                .body(streamResource);
    }

    @GetMapping("/eventos/")
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

    @GetMapping("/clientes")
    public ResponseEntity<?> listarUsuariosClientes() {
        try {
            Set<Usuario> usuarios = usuarioService.obtenerUsuarioClientes();
            if (usuarios.isEmpty()) {
                logger.warn("No se encontraron usuarios clientes.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "No se encontraron usuarios clientes."));
            }
            logger.info("Se encontraron {} usuarios clientes.", usuarios.size());
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            logger.error("Error al listar usuarios clientes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error inesperado al obtener usuarios clientes."));
        }
    }

}
