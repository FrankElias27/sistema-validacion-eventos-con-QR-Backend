package com.example.aos.sistema_aos_spring_boot.Controladores;


import com.example.aos.sistema_aos_spring_boot.Enums.Visibilidad;
import com.example.aos.sistema_aos_spring_boot.Modelo.*;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.SpecificServiceException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Request.VerificarAsistenciaRequest;
import com.example.aos.sistema_aos_spring_boot.Modelo.Request.QRCodeConfirmedRequest;
import com.example.aos.sistema_aos_spring_boot.Modelo.Request.QRCodeRequest;
import com.example.aos.sistema_aos_spring_boot.Servicios.BirthdayService;
import com.example.aos.sistema_aos_spring_boot.Servicios.EventoService;
import com.example.aos.sistema_aos_spring_boot.Servicios.QRCodeService;
import com.example.aos.sistema_aos_spring_boot.Servicios.RegistroService;
import com.google.zxing.WriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.security.Principal;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private EventoService eventoService;

    @Autowired
    private RegistroService registroService;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private BirthdayService birthdayService;

    @Autowired
    private UserDetailsService userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);

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

    @PostMapping("/generate")
    public CompletableFuture<ResponseEntity<String>> generateQRCode(@RequestBody QRCodeRequest qrCodeRequest) {

        return CompletableFuture.supplyAsync(() -> {
            Long event = qrCodeRequest.getEvent();
            String user = qrCodeRequest.getUser();
            String dni = qrCodeRequest.getDni();

            Evento evento = qrCodeService.getEvento(event);
            if (evento == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Evento no encontrado");
            }

            int cantidadQRActual = qrCodeService.countQRCodesForEvent(event);
            int cantidadQRMaxima = evento.getCantidadQR();

            if (cantidadQRActual >= cantidadQRMaxima) {
                eventoService.updateEventoVisibilidad(event, Visibilidad.SOLDOUT);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Límite de códigos QR alcanzado para este evento");
            }

            String qrCodeText = "EventID:" + event + ";UserID:" + user + ";Dni:" + dni;

            try {
                String qrCodeImage = qrCodeService.generateQRCodeImage(qrCodeText).join();
                return ResponseEntity.ok(qrCodeImage);
            } catch (WriterException e) {
                logger.error("Error al generar el código QR: {}", e.getMessage());

                throw new RuntimeException("Error al generar el código QR", e);
            } catch (IOException e) {
                logger.error("Error de entrada/salida: {}", e.getMessage());

                throw new RuntimeException("Error de entrada/salida", e);
            } catch (Exception e) {
                logger.error("Error inesperado en la función generateQRCode: {}", e.getMessage());

                throw e;
            }
        });
    }

    @PostMapping("/guardarqr")
    public ResponseEntity<QRCode> guardarQRCode(@RequestBody QRCode qrCode) {

        try {
            QRCode savedQRCode = qrCodeService.agregarQRCode(qrCode);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedQRCode);
        } catch (SpecificServiceException e) {
            logger.error("Error al guardar el código QR: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalArgumentException e) {
            logger.warn("Argumento inválido al guardar el código QR: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error inesperado al guardar el código QR: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/guardar-registro")
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

    @PostMapping("/birthday/agregar")
    public ResponseEntity<Map<String, String>> agregarCumpleaños(@RequestBody List<Birthday> nuevosCumpleaños, Principal principal) {

        if (nuevosCumpleaños.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("mensaje", "La lista de cumpleaños no puede estar vacía."));
        }

        Usuario usuarioActual = (Usuario) userDetailsService.loadUserByUsername(principal.getName());

        try {

            LocalDateTime createdDate = LocalDateTime.now();
            int añoActual = LocalDate.now().getYear();

            for (Birthday birthday : nuevosCumpleaños) {
                birthday.setCreatedDate(createdDate);
                birthday.setAño(añoActual);
                birthday.setUsuario(usuarioActual);
            }

            birthdayService.agregarCumpleaños(usuarioActual, nuevosCumpleaños);
            return ResponseEntity.ok(Collections.singletonMap("mensaje", "Cumpleaños agregados exitosamente."));
        } catch (SpecificServiceException e) {
            logger.error("Error al agregar cumpleaños: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("mensaje", "Error al agregar cumpleaños: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado al agregar cumpleaños: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("mensaje",  e.getMessage()));
        }
    }

    @GetMapping("/invitados")
    public ResponseEntity<List<Birthday>> obtenerInvitados(@AuthenticationPrincipal Usuario usuarioActual) {

        int añoActual = LocalDate.now().getYear();

        try {
            List<Birthday> invitados = birthdayService.findByUsuarioAndAño(usuarioActual, añoActual);
            return ResponseEntity.ok(invitados);
        } catch (SpecificServiceException e) {
            logger.error("Error al obtener invitados: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.emptyList());
        } catch (Exception e) {
            logger.error("Error inesperado al obtener invitados: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/obtener/{eventoId}")
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
}
