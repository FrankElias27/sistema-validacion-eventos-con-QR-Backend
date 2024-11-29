package com.example.aos.sistema_aos_spring_boot.Controladores;

import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.EmailLimitExceededException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.SpecificEmailException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.UserNotFoundException;
import com.example.aos.sistema_aos_spring_boot.Servicios.MailService;
import com.example.aos.sistema_aos_spring_boot.Servicios.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    private static final Logger logger = LoggerFactory.getLogger(MailController.class);

    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@RequestParam String email) {
        Map<String, String> response = new HashMap<>();

        try {
            mailService.sendPasswordResetEmail(email);
            response.put("message", "Por favor, revise su bandeja de entrada o correo no deseado.");
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            logger.warn("Intento de restablecer la contraseña para un email no registrado: {}", email);
            response.put("error", "No se encontró un usuario con ese correo electrónico.");
            return ResponseEntity.badRequest().body(response);
        } catch (EmailLimitExceededException e) {
            logger.warn("Límite de envío de correos excedido para: {}", email);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
        } catch (SpecificEmailException e) {
            logger.error("Error al enviar el correo a {}: {}", email, e.getMessage());
            response.put("error", "Error al enviar el correo. Intente nuevamente más tarde.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            logger.error("Error inesperado al solicitar el restablecimiento de contraseña para {}: {}", email, e.getMessage());
            response.put("error", "Ocurrió un error inesperado. Por favor, intente más tarde.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
