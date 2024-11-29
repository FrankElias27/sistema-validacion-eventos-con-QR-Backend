package com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e, WebRequest request) {
        // Obtener información del controlador o endpoint que produjo la falla
        String controllerName = request.getDescription(false); // Descripción de la solicitud
        logger.error("Error inesperado en {}: {}", controllerName, e.getMessage(), e);

        // Devolver una respuesta de error genérica
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ha ocurrido un error inesperado. Por favor, inténtelo de nuevo más tarde.");
    }
}
