package com.example.aos.sistema_aos_spring_boot.Controladores;


import com.example.aos.sistema_aos_spring_boot.Modelo.Birthday;
import com.example.aos.sistema_aos_spring_boot.Modelo.DTO.BirthdayDTO;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.SpecificServiceException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Usuario;
import com.example.aos.sistema_aos_spring_boot.Servicios.BirthdayService;
import com.example.aos.sistema_aos_spring_boot.Servicios.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/birthday")
public class BirthdayController {

    @Autowired
    private BirthdayService birthdayService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UsuarioService usuarioService;

    private static final Logger logger = LoggerFactory.getLogger(BirthdayController.class);

    @PostMapping("/agregar")
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
                    .body(Collections.singletonMap("mensaje", "Error inesperado: " + e.getMessage()));
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


    @GetMapping("/invitados/{usuarioId}")
    public ResponseEntity<List<Birthday>> obtenerInvitadosPorUsuario(@PathVariable Long usuarioId) {

        int añoActual = LocalDate.now().getYear();

        try {

            Usuario usuario = usuarioService.obtenerUsuarioporID(usuarioId);

            if (usuario == null) {
                logger.warn("Usuario no encontrado con ID: {}", usuarioId);
                return ResponseEntity.notFound().build();
            }

            List<Birthday> invitados = birthdayService.findByUsuarioAndAño(usuario, añoActual);
            return ResponseEntity.ok(invitados);
        } catch (SpecificServiceException e) {
            logger.error("Error al obtener los invitados para el usuario ID {}: {}", usuarioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        } catch (Exception e) {
            logger.error("Error inesperado al obtener los invitados para el usuario ID {}: {}", usuarioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @PutMapping("/actualizar/{usuarioId}")
    public ResponseEntity<Map<String, String>> actualizarCumpleaños(
            @PathVariable Long usuarioId,
            @RequestBody List<BirthdayDTO> cumpleañosActualizados) {

        Usuario usuarioActual = usuarioService.obtenerUsuarioporID(usuarioId);

        if (usuarioActual == null) {
            logger.warn("Usuario no encontrado con ID: {}", usuarioId);
            return ResponseEntity.notFound().build();
        }

        try {

            birthdayService.actualizarCumpleaños(usuarioId, cumpleañosActualizados);
            return ResponseEntity.ok(Collections.singletonMap("mensaje", "Lista de Cumpleaños actualizado exitosamente."));
        } catch (IllegalArgumentException e) {
            logger.warn("Error de argumento al actualizar cumpleaños para usuario ID {}: {}", usuarioId, e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("mensaje", e.getMessage()));
        } catch (SpecificServiceException e) {
            logger.error("Error al actualizar cumpleaños para usuario ID {}: {}", usuarioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("mensaje", "Error al actualizar los cumpleaños. Intente nuevamente más tarde."));
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar cumpleaños para usuario ID {}: {}", usuarioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("mensaje", "Ocurrió un error inesperado. Por favor, intente más tarde."));
        }
    }


}
