package com.example.aos.sistema_aos_spring_boot.Controladores;

import com.example.aos.sistema_aos_spring_boot.Modelo.*;
import com.example.aos.sistema_aos_spring_boot.Modelo.DTO.UsuarioDTO;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.InvalidCodeException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Request.PasswordChangeRequest;
import com.example.aos.sistema_aos_spring_boot.Repositorios.RolRepository;
import com.example.aos.sistema_aos_spring_boot.Repositorios.UsuarioRepository;
import com.example.aos.sistema_aos_spring_boot.Repositorios.VerificationCodeRepository;
import com.example.aos.sistema_aos_spring_boot.Servicios.MailService;
import com.example.aos.sistema_aos_spring_boot.Servicios.RolService;
import com.example.aos.sistema_aos_spring_boot.Servicios.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private RolService rolService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private MailService mailService;

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    @PostMapping("/")
    public ResponseEntity<?> guardarUsuario(@RequestBody Usuario usuario) {
        try {
            if (usuario == null) {
                throw new IllegalArgumentException("El usuario no puede ser nulo.");
            }
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                throw new IllegalArgumentException("La contraseña es obligatoria.");
            }

            usuario.setFechaRegistro(LocalDateTime.now());
            usuario.setVerified(true);
            usuario.setPassword(this.bCryptPasswordEncoder.encode(usuario.getPassword()));

            Set<UsuarioRol> usuarioRoles = new HashSet<>();
            Rol rol = new Rol(2L, "ROLE_USUARIO");

            UsuarioRol usuarioRol = new UsuarioRol();
            usuarioRol.setUsuario(usuario);
            usuarioRol.setRol(rol);
            usuarioRoles.add(usuarioRol);

            Usuario savedUser = usuarioService.guardarUsuario(usuario, usuarioRoles);
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            logger.error("Error de argumento: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Error en los datos proporcionados."));
        } catch (DataIntegrityViolationException e) {
            logger.error("Error de integridad de datos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("message", "El usuario ya existe o hay un conflicto en los datos."));
        } catch (Exception e) {
            logger.error("Error inesperado al guardar el usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/portero")
    public ResponseEntity<?> guardarUsuarioPortero(@RequestBody Usuario usuario) {
        try {
            if (usuario == null) {
                throw new IllegalArgumentException("El usuario no puede ser nulo.");
            }
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                throw new IllegalArgumentException("La contraseña es obligatoria.");
            }

            usuario.setFechaRegistro(LocalDateTime.now());
            usuario.setVerified(true);
            usuario.setPassword(this.bCryptPasswordEncoder.encode(usuario.getPassword()));

            Set<UsuarioRol> usuarioRoles = new HashSet<>();
            Rol rol = new Rol(3L, "ROLE_PORTERO");

            UsuarioRol usuarioRol = new UsuarioRol();
            usuarioRol.setUsuario(usuario);
            usuarioRol.setRol(rol);
            usuarioRoles.add(usuarioRol);

            Usuario savedUser = usuarioService.guardarUsuario(usuario, usuarioRoles);
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            logger.error("Error de argumento: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Error en los datos proporcionados."));
        } catch (DataIntegrityViolationException e) {
            logger.error("Error de integridad de datos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("message", "El usuario ya existe o hay un conflicto en los datos."));
        } catch (Exception e) {
            logger.error("Error inesperado al guardar el usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/admin")
    public ResponseEntity<?> guardarUsuarioAdmin(@RequestBody Usuario usuario) {
        try {
            if (usuario == null) {
                throw new IllegalArgumentException("El usuario no puede ser nulo.");
            }
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                throw new IllegalArgumentException("La contraseña es obligatoria.");
            }

            usuario.setFechaRegistro(LocalDateTime.now());
            usuario.setVerified(true);
            usuario.setPassword(this.bCryptPasswordEncoder.encode(usuario.getPassword()));

            Set<UsuarioRol> usuarioRoles = new HashSet<>();
            Rol rol = new Rol(4L, "ROLE_ADMIN-JUNIOR");

            UsuarioRol usuarioRol = new UsuarioRol();
            usuarioRol.setUsuario(usuario);
            usuarioRol.setRol(rol);
            usuarioRoles.add(usuarioRol);

            Usuario savedUser = usuarioService.guardarUsuario(usuario, usuarioRoles);
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            logger.error("Error de argumento: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Error en los datos proporcionados."));
        } catch (DataIntegrityViolationException e) {
            logger.error("Error de integridad de datos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("message", "El usuario ya existe o hay un conflicto en los datos."));
        } catch (Exception e) {
            logger.error("Error inesperado al guardar el usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/admin-sistema")
    public ResponseEntity<?> guardarUsuarioAdminSistema(@RequestBody Usuario usuario) {
        try {
            if (usuario == null) {
                throw new IllegalArgumentException("El usuario no puede ser nulo.");
            }
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                throw new IllegalArgumentException("La contraseña es obligatoria.");
            }

            usuario.setFechaRegistro(LocalDateTime.now());
            usuario.setVerified(true);
            usuario.setPassword(this.bCryptPasswordEncoder.encode(usuario.getPassword()));

            Set<UsuarioRol> usuarioRoles = new HashSet<>();
            Rol rol = new Rol(1L, "ROLE_ADMIN");

            UsuarioRol usuarioRol = new UsuarioRol();
            usuarioRol.setUsuario(usuario);
            usuarioRol.setRol(rol);
            usuarioRoles.add(usuarioRol);

            Usuario savedUser = usuarioService.guardarUsuario(usuario, usuarioRoles);
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            logger.error("Error de argumento: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Error en los datos proporcionados."));
        } catch (DataIntegrityViolationException e) {
            logger.error("Error de integridad de datos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("message", "El usuario ya existe o hay un conflicto en los datos."));
        } catch (Exception e) {
            logger.error("Error inesperado al guardar el usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUsuario(@RequestBody Usuario usuario) {
        Usuario nuevoUsuario = null;

        if (usuario == null || usuario.getEmail() == null || usuario.getPassword() == null) {
            logger.error("El usuario, el correo electrónico o la contraseña son nulos.");
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Usuario, correo electrónico y contraseña son obligatorios."));
        }
        try {

            if (usuarioService.existeUsuario(usuario.getEmail())) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "El correo electrónico ya está registrado. Por favor, elija uno diferente."));
            }

            nuevoUsuario = usuarioService.guardarUsuarioConRolDeUser(usuario, 2L, "ROLE_USUARIO");
            mailService.sendVerificationEmail(nuevoUsuario.getEmail());

            return ResponseEntity.ok(nuevoUsuario);
        } catch (MailException e) {
            logger.error("Error al enviar el correo de verificación: {}", e.getMessage());

            if (nuevoUsuario != null) {
                usuarioService.eliminarUsuario(nuevoUsuario.getId());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "No se pudo enviar el correo de verificación. Por favor, inténtelo más tarde."));
        } catch (DataIntegrityViolationException e) {
            logger.error("Error de integridad de datos: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("message", "Error de integridad de datos. Por favor, verifique la información proporcionada."));
        } catch (Exception e) {
            logger.error("Error inesperado al registrar el usuario: {}", e.getMessage());

            if (nuevoUsuario != null) {
                usuarioService.eliminarUsuario(nuevoUsuario.getId());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message",  e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody Map<String, String> request) {
        String code = request.get("code");

        if (code == null || code.isEmpty()) {
            logger.error("El código de verificación es nulo o vacío.");
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "El código de verificación es obligatorio."));
        }

        try {

            VerificationCodeEntity verificationCodeEntity = verificationCodeRepository.findByCode(code);
            if (verificationCodeEntity == null) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Código de verificación no válido."));
            }

            if (LocalDateTime.now().isAfter(verificationCodeEntity.getExpirationTime())) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "El código de verificación ha expirado. Contacta al soporte técnico +51 970 932 182."));
            }

            Usuario usuario = verificationCodeEntity.getUsuario();
            usuario.setVerified(true);
            usuarioService.save(usuario);

            verificationCodeRepository.delete(verificationCodeEntity);

            return ResponseEntity.ok(Collections.singletonMap("message", "¡Verificación Exitosa!"));
        } catch (DataAccessException e) {
            logger.error("Error al acceder a la base de datos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error en el acceso a la base de datos. Intente nuevamente más tarde."));
        } catch (Exception e) {
            logger.error("Error inesperado durante la verificación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error interno del servidor. Intente nuevamente más tarde."));
        }
    }



    @GetMapping("/{username}")
    public ResponseEntity<?> obtenerUsuario(@PathVariable("username") String username) {

        if (username == null || username.isEmpty()) {
            logger.error("El nombre de usuario es nulo o vacío.");
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "El nombre de usuario es obligatorio."));
        }

        try {
            Usuario usuario = usuarioService.obtenerUsuario(username);
            if (usuario == null) {
                logger.warn("Usuario no encontrado: {}", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "Usuario no encontrado"));
            }
            logger.info("Usuario encontrado: {}", username);
            return ResponseEntity.ok(usuario);
        } catch (DataAccessException e) {
            logger.error("Error al acceder a la base de datos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error en el acceso a la base de datos. Intente nuevamente más tarde."));
        } catch (Exception e) {
            logger.error("Error inesperado al obtener el usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error interno del servidor. Intente nuevamente más tarde."));
        }
    }

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable("usuarioId") Long usuarioId) {

        if (usuarioId == null || usuarioId <= 0) {
            logger.error("El ID de usuario es nulo o inválido: {}", usuarioId);
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "El ID de usuario es obligatorio y debe ser mayor que cero."));
        }

        try {
            usuarioService.eliminarUsuario(usuarioId);

            logger.info("Usuario eliminado con éxito: {}", usuarioId);
            return ResponseEntity.ok(Collections.singletonMap("message", "Usuario eliminado con éxito"));
        } catch (IllegalStateException e) {

            logger.warn("Intento de eliminar un usuario con registros asociados: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("message", e.getMessage()));
        } catch (NoSuchElementException e) {
            logger.warn("Usuario no encontrado al intentar eliminar: {}", usuarioId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Usuario no encontrado"));
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar el usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error interno del servidor. Intente nuevamente más tarde."));
        }
    }

    @GetMapping("/page/{role}/{page}")
    public ResponseEntity<?> listarUsuariosPorRol(
            @PathVariable("role") String role,
            @PathVariable("page") int page) {

        if (page < 0) {
            logger.error("La página no puede ser negativa: {}", page);
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "La página no puede ser negativa"));
        }

        Long roleId = obtenerRoleId(role);
        if (roleId == null) {
            logger.warn("Rol no encontrado para el nombre: {}", role);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Rol no encontrado"));
        }

        Rol rol = rolService.findById(roleId);
        if (rol == null) {
            logger.warn("Rol no encontrado en la base de datos para el ID: {}", roleId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Rol no encontrado"));
        }

        List<Usuario> usuariosConRol;
        try {
            usuariosConRol = usuarioService.obtenerUsuariosPorRol(rol);
        } catch (Exception e) {
            logger.error("Error al obtener usuarios por rol: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error interno al obtener usuarios"));
        }

        usuariosConRol.sort((u1, u2) -> u2.getId().compareTo(u1.getId()));

        int pageSize = 10;
        int totalUsuarios = usuariosConRol.size();
        int start = Math.min(page * pageSize, totalUsuarios);
        int end = Math.min(start + pageSize, totalUsuarios);

        if (start >= totalUsuarios && page > 0) {
            logger.warn("No se encontraron usuarios en la página solicitada: {}, total: {}", page, totalUsuarios);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "No se encontraron usuarios en la página solicitada"));
        }

        List<Usuario> usuariosEnPagina = usuariosConRol.subList(start, end);
        Page<Usuario> usuariosPage = new PageImpl<>(usuariosEnPagina, PageRequest.of(page, pageSize), totalUsuarios);

        logger.info("Usuarios listados para el rol: {}, página: {}", role, page);
        return ResponseEntity.ok(usuariosPage);
    }

    private Long obtenerRoleId(String role) {
        if ("admins".equalsIgnoreCase(role)) {
            return 4L;
        } else if ("usuarios".equalsIgnoreCase(role)) {
            return 2L;
        } else if ("porteros".equalsIgnoreCase(role)) {
            return 3L;
        } else if ("admins-sistema".equalsIgnoreCase(role)) {
            return 1L;
        }else {
            return null;
        }
    }


    @GetMapping("/id/{usuarioId}")
    public ResponseEntity<?> listarUsuarioPorId(@PathVariable("usuarioId") Long usuarioId) {

        if (usuarioId == null || usuarioId <= 0) {
            logger.error("El ID de usuario es nulo o inválido: {}", usuarioId);
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "El ID de usuario es obligatorio y debe ser mayor que cero."));
        }

        try {
            Usuario usuario = usuarioService.obtenerUsuarioporID(usuarioId);
            logger.info("Usuario encontrado: {}", usuarioId);
            return ResponseEntity.ok(usuario);
        } catch (NoSuchElementException e) {
            logger.warn("Usuario no encontrado: {}", usuarioId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Usuario con ID " + usuarioId + " no encontrado"));
        } catch (Exception e) {
            logger.error("Error inesperado al obtener el usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error inesperado: " + e.getMessage()));
        }
    }

    @PutMapping("/update-status")
    public ResponseEntity<?> actualizarUsuario(@RequestBody Map<String, Object> updates) {

        if (!updates.containsKey("id") || !updates.containsKey("enabled")) {
            logger.error("Faltan parámetros necesarios: {}", updates);
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Faltan parámetros necesarios."));
        }

        try {
            Long usuarioId = Long.parseLong(updates.get("id").toString());
            Boolean enabled = Boolean.parseBoolean(updates.get("enabled").toString());

            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new NoSuchElementException("Usuario con ID " + usuarioId + " no encontrado"));

            usuario.setEnabled(enabled);
            Usuario usuarioActualizado = usuarioRepository.save(usuario);

            logger.info("Usuario actualizado: {}", usuarioId);
            return ResponseEntity.ok(usuarioActualizado);

        } catch (NoSuchElementException e) {
            logger.warn("Intento de actualizar un usuario no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", e.getMessage()));
        } catch (NumberFormatException e) {
            logger.error("ID inválido proporcionado: {}", updates.get("id"));
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "ID inválido."));
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar el usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error inesperado: " + e.getMessage()));
        }
    }

    @PutMapping("/update-verification-status")
    public ResponseEntity<?> actualizarVerificacionUsuario(@RequestBody Map<String, Object> updates) {

        if (!updates.containsKey("id") || !updates.containsKey("verified")) {
            logger.error("Faltan parámetros necesarios: {}", updates);
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Faltan parámetros necesarios."));
        }

        try {
            Long usuarioId = Long.parseLong(updates.get("id").toString());
            Boolean verified = Boolean.parseBoolean(updates.get("verified").toString());

            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new NoSuchElementException("Usuario con ID " + usuarioId + " no encontrado"));

            usuario.setVerified(verified);

            Usuario usuarioActualizado = usuarioRepository.save(usuario);
            logger.info("Usuario verificado actualizado: {}", usuarioId);
            return ResponseEntity.ok(usuarioActualizado);

        } catch (NoSuchElementException e) {
            logger.warn("Intento de actualizar un usuario no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", e.getMessage()));
        } catch (NumberFormatException e) {
            logger.error("ID inválido proporcionado: {}", updates.get("id"));
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "ID inválido."));
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar la verificación del usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error inesperado: " + e.getMessage()));
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarUsuarioPorNombre(
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        if (page < 0 || size <= 0) {
            logger.error("Página o tamaño inválidos: page={}, size={}", page, size);
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "La página y el tamaño deben ser mayores a 0."));
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Usuario> usuariosEncontrados = usuarioService.buscarUsuarioPorNombre(nombre, pageable);

            if (usuariosEncontrados.isEmpty()) {
                logger.warn("No se encontraron usuarios con el nombre: {}", nombre);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "No se encontraron usuarios con el nombre proporcionado."));
            }

            logger.info("Usuarios encontrados: {}", usuariosEncontrados.getTotalElements());
            return ResponseEntity.ok(usuariosEncontrados);
        } catch (Exception e) {
            logger.error("Error inesperado al buscar usuarios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error inesperado: " + e.getMessage()));
        }
    }

    @GetMapping("/buscar/sistema")
    public ResponseEntity<?> buscarSistemaPorNombre(
            @RequestParam("nombre") String nombre,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<Usuario> usuariosEncontrados = usuarioService.buscarSistemaPorNombre(nombre, pageable);

            if (usuariosEncontrados.isEmpty()) {
                logger.warn("No se encontraron administradores con el nombre: {}", nombre);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "No se encontraron administradores con el nombre proporcionado."));
            }

            logger.info("Administradores encontrados: {}", usuariosEncontrados.getTotalElements());
            return ResponseEntity.ok(usuariosEncontrados);
        } catch (Exception e) {
            logger.error("Error inesperado al buscar administradores: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error inesperado: " + e.getMessage()));
        }
    }

    @GetMapping("/buscar/admin")
    public ResponseEntity<?> buscarAdminPorNombre(
            @RequestParam("nombre") String nombre,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<Usuario> usuariosEncontrados = usuarioService.buscarAdminPorNombre(nombre, pageable);

            if (usuariosEncontrados.isEmpty()) {
                logger.warn("No se encontraron administradores con el nombre: {}", nombre);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "No se encontraron administradores con el nombre proporcionado."));
            }

            logger.info("Administradores encontrados: {}", usuariosEncontrados.getTotalElements());
            return ResponseEntity.ok(usuariosEncontrados);
        } catch (Exception e) {
            logger.error("Error inesperado al buscar administradores: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error inesperado: " + e.getMessage()));
        }
    }


    @GetMapping("/buscar/portero")
    public ResponseEntity<?> buscarPorteroPorNombre(
            @RequestParam("nombre") String nombre,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<Usuario> usuariosEncontrados = usuarioService.buscarPorteroPorNombre(nombre, pageable);

            if (usuariosEncontrados.isEmpty()) {
                logger.warn("No se encontraron porteros con el nombre: {}", nombre);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "No se encontraron porteros con el nombre proporcionado."));
            }

            logger.info("Porteros encontrados: {}", usuariosEncontrados.getTotalElements());
            return ResponseEntity.ok(usuariosEncontrados);
        } catch (Exception e) {
            logger.error("Error inesperado al buscar porteros: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error inesperado: " + e.getMessage()));
        }
    }

    @GetMapping("/buscar/usuario")
    public ResponseEntity<?> buscarUsuarioPorNombreCliente(
            @RequestParam("nombre") String nombre,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<Usuario> usuariosEncontrados = usuarioService.buscarClientePorNombre(nombre, pageable);

            if (usuariosEncontrados.isEmpty()) {
                logger.warn("No se encontraron usuarios con el nombre: {}", nombre);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "No se encontraron usuarios con el nombre proporcionado."));
            }

            logger.info("Usuarios encontrados: {}", usuariosEncontrados.getTotalElements());
            return ResponseEntity.ok(usuariosEncontrados);
        } catch (Exception e) {
            logger.error("Error inesperado al buscar usuarios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error inesperado: " + e.getMessage()));
        }
    }



    @GetMapping("/birthday-hoy")
    public ResponseEntity<?> contarUsuariosCumpleanosHoy() {
        LocalDate hoy = LocalDate.now();
        try {
            long conteoCumpleanosHoy = usuarioService.contarUsuariosCumpleanosHoyPorRol(2L, hoy.getDayOfMonth(), hoy.getMonthValue());
            logger.info("Cantidad de usuarios con cumpleaños hoy: {}", conteoCumpleanosHoy);
            return ResponseEntity.ok((int) conteoCumpleanosHoy);
        } catch (DataAccessException e) {
            logger.error("Error al acceder a la base de datos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error al acceder a la base de datos."));
        } catch (Exception e) {
            logger.error("Error inesperado al contar usuarios con cumpleaños hoy: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error inesperado: " + e.getMessage()));
        }
    }

    @GetMapping("/page/cumpleanos/{page}")
    public ResponseEntity<?> listarUsuariosPorRolUsuarioYCumpleaños(@PathVariable("page") int page) {
        if (page < 0) {
            logger.warn("Número de página negativo recibido: {}", page);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Número de página no puede ser negativo."));
        }

        Long roleId = 2L;

        Rol rol = rolService.findById(roleId);
        if (rol == null) {
            logger.warn("Rol no encontrado para ID: {}", roleId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Rol no encontrado."));
        }

        LocalDate hoy = LocalDate.now();

        try {
            Page<Usuario> usuariosPage = usuarioService.encontrarUsuariosPorRolYCumpleaños(roleId, hoy.getDayOfMonth(), hoy.getMonthValue(), PageRequest.of(page, 10));
            if (usuariosPage.isEmpty()) {
                logger.info("No se encontraron usuarios en la página: {}", page);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "No se encontraron usuarios en la página solicitada."));
            }
            logger.info("Usuarios encontrados en la página {}: {}", page, usuariosPage.getTotalElements());
            return ResponseEntity.ok(usuariosPage);
        } catch (Exception e) {
            logger.error("Error al obtener usuarios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error al obtener usuarios."));
        }
    }

    @GetMapping("/cumpleanos/rango/{page}")
    public ResponseEntity<?> listarUsuariosPorCumpleanosEntreFechas(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @PathVariable int page,
            @RequestParam(defaultValue = "10") int size) {

        if (fechaInicio.isAfter(fechaFin)) {
            logger.warn("La fecha de inicio es después de la fecha de fin: {} a {}", fechaInicio, fechaFin);
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "La fecha de inicio no puede ser después de la fecha de fin."));
        }

        if (page < 0) {
            logger.info("Número de página negativo recibido, se ajustará a 0.");
            page = 0;
        }

        Long roleId = 2L;

        try {
            Page<Usuario> usuariosPage = usuarioService.encontrarUsuariosPorRolYFechas(roleId, fechaInicio, fechaFin, PageRequest.of(page, size));

            if (usuariosPage.isEmpty()) {
                logger.info("No se encontraron usuarios con cumpleaños entre {} y {} y rol de usuario.", fechaInicio, fechaFin);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", "No se encontraron usuarios con cumpleaños entre las fechas especificadas y rol de usuario."));
            }

            logger.info("Se encontraron {} usuarios con cumpleaños entre {} y {} en la página {}.", usuariosPage.getTotalElements(), fechaInicio, fechaFin, page);
            return ResponseEntity.ok(usuariosPage);
        } catch (Exception e) {
            logger.error("Error al obtener usuarios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", "Error al obtener usuarios."));
        }
    }



    @PutMapping("/update-details")
    public ResponseEntity<?> actualizarUsuarioDetalles(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        Long usuarioId = usuarioDTO.getId();
        if (usuarioId == null) {
            logger.warn("El ID del usuario es nulo.");
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "El ID del usuario es requerido y debe ser válido."));
        }

        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new NoSuchElementException("Usuario con ID " + usuarioId + " no encontrado"));

            usuarioService.updateUsuario(usuarioDTO, usuario);

            Usuario usuarioActualizado = usuarioRepository.save(usuario);
            logger.info("Usuario con ID {} actualizado exitosamente.", usuarioId);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (NoSuchElementException e) {
            logger.warn("Error al buscar usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error en la actualización: " + e.getMessage()));
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

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody PasswordChangeRequest request) {
        Map<String, String> response = new HashMap<>();

        try {

            usuarioService.validateCode(request.getCode());

            usuarioService.changePassword(request.getCode(), request.getNewPassword());
            response.put("message", "Contraseña cambiada con éxito.");
            logger.info("Contraseña cambiada para el código: {}", request.getCode());
            return ResponseEntity.ok(response);
        } catch (InvalidCodeException e) {
            response.put("error", "Código de validación inválido.");
            logger.warn("Intento de cambio de contraseña fallido: {}", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", "Error inesperado: " + e.getMessage());
            logger.error("Error al cambiar la contraseña: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


}

