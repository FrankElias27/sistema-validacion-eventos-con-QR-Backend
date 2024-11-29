package com.example.aos.sistema_aos_spring_boot.Controladores;
import com.example.aos.sistema_aos_spring_boot.Enums.Visibilidad;
import com.example.aos.sistema_aos_spring_boot.Modelo.Evento;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.SpecificServiceException;
import com.example.aos.sistema_aos_spring_boot.Modelo.QRCode;
import com.example.aos.sistema_aos_spring_boot.Modelo.Request.QRCodeValidationResult;
import com.example.aos.sistema_aos_spring_boot.Modelo.Request.QRCodeConfirmedRequest;
import com.example.aos.sistema_aos_spring_boot.Modelo.Request.QRCodeRequest;
import com.example.aos.sistema_aos_spring_boot.Modelo.Request.ValorQRRequest;
import com.example.aos.sistema_aos_spring_boot.Servicios.EventoService;
import com.example.aos.sistema_aos_spring_boot.Servicios.QRCodeService;
import com.google.zxing.WriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/qrcode")
public class QRCodeController {

    private final QRCodeService qrCodeService;
    private final PasswordEncoder passwordEncoder;
    private final EventoService eventoService;

    private static final Logger logger = LoggerFactory.getLogger(QRCodeController.class);

    public QRCodeController(QRCodeService qrCodeService, PasswordEncoder passwordEncoder, EventoService eventoService) {
        this.qrCodeService = qrCodeService;
        this.passwordEncoder = passwordEncoder;
        this.eventoService = eventoService;
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

    @PostMapping("/")
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


}