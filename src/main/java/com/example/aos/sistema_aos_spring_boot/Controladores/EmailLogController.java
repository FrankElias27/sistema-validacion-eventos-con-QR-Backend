package com.example.aos.sistema_aos_spring_boot.Controladores;

import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.DatabaseAccessException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.EmailLogException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.LogsNoEncontradosException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.SpecificServiceException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Usuario;
import com.example.aos.sistema_aos_spring_boot.Servicios.EmailLogService;
import com.example.aos.sistema_aos_spring_boot.Servicios.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email-logs")
public class EmailLogController {

    @Autowired
    private EmailLogService emailLogService;

    @Autowired
    private UsuarioService usuarioService;

    private static final Logger logger = LoggerFactory.getLogger(EmailLogController.class);

    @DeleteMapping("/usuario/{usuarioId}")
    public ResponseEntity<String> deleteEmailLogsByUsuarioId(@PathVariable Long usuarioId) {
        try {
            Usuario usuario = usuarioService.obtenerUsuarioporID(usuarioId);
            if (usuario == null) {
                return ResponseEntity.notFound().build();
            }

            emailLogService.deleteEmailLogsByUsuarioId(usuarioId);
            return ResponseEntity.noContent().build();
        } catch (LogsNoEncontradosException e) {
            logger.error("No se encontraron logs para el usuario {}: {}", usuarioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SpecificServiceException e) {
            logger.error("Error al eliminar los registros de email para el usuario {}: {}", usuarioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar los registros de email para el usuario {}: {}", usuarioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/usuario/{usuarioId}/hoy")
    public ResponseEntity<String> eliminarRegistrosDeHoy(@PathVariable Long usuarioId) {
        try {
            Usuario usuario = usuarioService.obtenerUsuarioporID(usuarioId);
            if (usuario == null) {
                return ResponseEntity.notFound().build();
            }
            emailLogService.eliminarRegistrosDeHoy(usuario);
            return ResponseEntity.noContent().build();

        } catch (LogsNoEncontradosException e) {
            logger.error("No se encontraron logs para el usuario {}: {}", usuarioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (DatabaseAccessException e) {
            logger.error("Error al acceder a la base de datos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (EmailLogException e) {
            logger.error("Error al eliminar registros de hoy: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (Exception e) {
            logger.error("Error inesperado al eliminar registros de hoy: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}