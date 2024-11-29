package com.example.aos.sistema_aos_spring_boot.Servicios.impl;

import com.example.aos.sistema_aos_spring_boot.Modelo.*;
import com.example.aos.sistema_aos_spring_boot.Modelo.DTO.UsuarioDTO;
import com.example.aos.sistema_aos_spring_boot.Repositorios.*;
import com.example.aos.sistema_aos_spring_boot.Servicios.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRolRepoitory usuarioRolRepository;

    @Autowired
    private RegistroRepository registroEventosRepository;

    @Autowired
    private BirthdayRepository birthdayRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private  ResetPasswordRepository resetPasswordRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private EmailLogRepository emailLogRepository;

    @Override
    @Transactional
    public Usuario guardarUsuario(Usuario usuario, Set<UsuarioRol> usuarioRoles) throws Exception {

        validarUsuario(usuario);

        Usuario usuarioLocal = usuarioRepository.findByUsername(usuario.getUsername());
        if (usuarioLocal != null) {
            System.out.println("El usuario ya existe");
            throw new Exception("El username ya está registrado. Por favor, elija un username diferente.");
        }

        Usuario usuarioPorCorreo = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioPorCorreo != null) {
            System.out.println("El correo ya está registrado");
            throw new Exception("El correo electrónico ya está registrado. Por favor, elija uno diferente.");
        }

        if (!usuario.getEmail().endsWith("@gmail.com") && !usuario.getEmail().endsWith("@hotmail.com")) {
            System.out.println("Dominio de correo no permitido");
            throw new Exception("El correo electrónico debe ser de dominio @gmail.com o @hotmail.com.");
        }

        Usuario usuarioPorDni = usuarioRepository.findByDNI(usuario.getDNI());
        if (usuarioPorDni != null) {
            System.out.println("El DNI ya está registrado");
            throw new Exception("El Dni ya está registrado. Para recibir soporte, envía un mensaje al +51 970 932 182.");
        }

        for (UsuarioRol usuarioRol : usuarioRoles) {
            rolRepository.save(usuarioRol.getRol());
        }

        usuario.getUsuarioRoles().addAll(usuarioRoles);
        usuarioLocal = usuarioRepository.save(usuario);
        return usuarioLocal;
    }

    private void validarUsuario(Usuario usuario) throws Exception {

        if (usuario.getUsername() == null ||
                usuario.getUsername().length() < 8 ||
                usuario.getUsername().length() > 20 ||
                !usuario.getUsername().matches("[a-zA-Z0-9]+")) { // Solo permite letras y números
            throw new Exception("El nombre de usuario debe tener al menos 8 caracteres y solo puede contener letras y números.");
        }


        if (usuario.getNombre() == null ||
                usuario.getNombre().isEmpty() ||
                usuario.getNombre().length() > 20 ||
                !usuario.getNombre().matches("[a-zA-Z\\s]+")) { // Permite letras y espacios
            throw new Exception("El nombre debe contener solo letras y espacios, y tener un máximo de 20 caracteres.");
        }


        if (usuario.getApellidoPaterno() == null ||
                usuario.getApellidoPaterno().isEmpty() ||
                usuario.getApellidoPaterno().length() > 20 ||
                !usuario.getApellidoPaterno().matches("[a-zA-Z\\s]+")) { // Permite letras y espacios
            throw new Exception("El apellido paterno debe contener solo letras y espacios, y tener un máximo de 20 caracteres.");
        }


        if (usuario.getApellidoMaterno() == null ||
                usuario.getApellidoMaterno().isEmpty() ||
                usuario.getApellidoMaterno().length() > 20 ||
                !usuario.getApellidoMaterno().matches("[a-zA-Z\\s]+")) { // Permite letras y espacios
            throw new Exception("El apellido materno debe contener solo letras y espacios, y tener un máximo de 20 caracteres.");
        }


        if (usuario.getDNI() == null || !usuario.getDNI().matches("\\d{8}")) { // Suponiendo que el DNI es de 8 dígitos
            throw new Exception("El DNI debe tener 8 dígitos.");
        }


        if (usuario.getFechaNacimiento() == null || !esMayorDeEdad(usuario.getFechaNacimiento())) {
            throw new Exception("El usuario debe ser mayor de 18 años.");
        }

        if (usuario.getTelefono() != null && !usuario.getTelefono().matches("\\d{9}")) { // Suponiendo que el teléfono tiene entre 9 y 15 dígitos
            throw new Exception("El teléfono debe contener solo números y tener 9 dígitos.");
        }
    }

    private boolean esMayorDeEdad(LocalDate fechaNacimiento) {
        LocalDate hoy = LocalDate.now();
        return hoy.minusYears(18).isAfter(fechaNacimiento);
    }

    @Override
    @Cacheable(value = "cumpleanosPorRol", key = "#roleId + '_' + #dia + '_' + #mes")
    public long contarUsuariosCumpleanosHoyPorRol(Long roleId, int dia, int mes) {
        return usuarioRepository.contarUsuariosCumpleanosHoyPorRol(roleId, dia, mes);
    }

    @Override
    @Cacheable(value = "usuariosPorRolYCumpleanos", key = "#roleId + '_' + #dia + '_' + #mes + '_' + #pageable.pageNumber")
    public Page<Usuario> encontrarUsuariosPorRolYCumpleaños(Long roleId, int dia, int mes, Pageable pageable) {
        return usuarioRepository.encontrarUsuariosPorRolYCumpleaños(roleId, dia, mes, pageable);
    }

    @Override
    public Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long usuarioId) {

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        verificationCodeRepository.deleteByUsuarioId(usuarioId);

        long eventosCount = registroEventosRepository.countByUsuarioId(usuarioId);
        long birthdayCount = birthdayRepository.countByUsuarioId(usuarioId);
        long LogsCount = emailLogRepository.countByUsuarioId(usuarioId);

        if (eventosCount > 0 || birthdayCount > 0 || LogsCount > 0 ) {
            throw new IllegalStateException("No se puede eliminar el usuario porque tiene registros asociados en eventos o cumpleaños o logs de Email.");
        }

        usuarioRepository.deleteById(usuarioId);
    }

    @Override
    public List<Usuario> obtenerUsuariosPorRol(Rol rol) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo.");
        }
        return usuarioRolRepository.findByRol(rol).stream()
                .map(UsuarioRol::getUsuario)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Page<Usuario> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    @Override
    public Usuario obtenerUsuarioporID(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Override
    public List<Usuario> obtenerUsuariosPorRolYCumpleaños(Rol rol, LocalDate fecha) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo.");
        }
        return obtenerUsuariosPorRol(rol).stream()
                .filter(usuario -> usuario.getFechaNacimiento().getDayOfMonth() == fecha.getDayOfMonth() &&
                        usuario.getFechaNacimiento().getMonth() == fecha.getMonth())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Usuario actualizarUsuario(Usuario usuario) {
        if (!usuarioRepository.existsById(usuario.getId())) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public Page<Usuario> buscarUsuarioPorNombre(String nombre, Pageable pageable) {
        return usuarioRepository.findByNombreContaining(nombre, pageable);
    }

    @Override
    public Page<Usuario> buscarSistemaPorNombre(String nombre, Pageable pageable) {
        return buscarPorRolYNombre(nombre, pageable, 1L);
    }

    @Override
    public Page<Usuario> buscarAdminPorNombre(String nombre, Pageable pageable) {
        return buscarPorRolYNombre(nombre, pageable, 4L);
    }

    @Override
    public Page<Usuario> buscarClientePorNombre(String nombre, Pageable pageable) {
        return buscarPorRolYNombre(nombre, pageable, 2L);
    }

    @Override
    public Page<Usuario> buscarPorteroPorNombre(String nombre, Pageable pageable) {
        return buscarPorRolYNombre(nombre, pageable, 3L);
    }

    private Page<Usuario> buscarPorRolYNombre(String nombreCompleto, Pageable pageable, Long rolId) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        List<Usuario> usuariosPorRol = obtenerUsuariosPorRol(rol);

        String[] palabras = nombreCompleto.toLowerCase().split(" ");

        List<Usuario> usuariosFiltrados = usuariosPorRol.stream()
                .filter(usuario ->
                        Arrays.stream(palabras).anyMatch(palabra ->
                                usuario.getNombre().toLowerCase().contains(palabra) ||
                                        usuario.getApellidoPaterno().toLowerCase().contains(palabra) ||
                                        usuario.getApellidoMaterno().toLowerCase().contains(palabra)
                        )
                )
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), usuariosFiltrados.size());
        List<Usuario> pagedList = usuariosFiltrados.subList(start, end);

        return new PageImpl<>(pagedList, pageable, usuariosFiltrados.size());
    }

    @Override
    public Page<Usuario> encontrarUsuariosPorRolYFechas(Long roleId, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {

        return usuarioRepository.findByRolIdAndCumpleanoBetween(roleId, fechaInicio, fechaFin, pageable);
    }

    @Override
    public Set<Usuario> obtenerUsuarioClientes() {
        return usuarioRepository.findUsuariosByRolId(2L);
    }

    @Override
    public boolean existeUsuario(String email) {
        return usuarioRepository.findByEmail(email) != null;
    }

    @Override
    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }



    @Override
    public void validateCode(String code) throws Exception {
        ResetPasswordEntity resetPasswordEntity = resetPasswordRepository.findByCode(code);
        if (resetPasswordEntity == null || resetPasswordEntity.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new Exception("Código de restablecimiento inválido o ha caducado.");
        }
    }

    @Override
    public void changePassword(String code, String newPassword) throws Exception {
        ResetPasswordEntity resetPasswordEntity = resetPasswordRepository.findByCode(code);
        if (resetPasswordEntity != null) {
            Usuario usuario = resetPasswordEntity.getUsuario();
            usuario.setPassword(bCryptPasswordEncoder.encode(newPassword));
            usuarioRepository.save(usuario);

            resetPasswordRepository.delete(resetPasswordEntity);
        } else {
            throw new Exception("Código de restablecimiento inválido.");
        }
    }

    @Override
    @Transactional
    public Usuario guardarUsuarioConRolDeUser(Usuario usuario, Long rolId, String rolNombre) throws Exception {

        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }
        if (rolId == null || rolNombre == null || rolNombre.isEmpty()) {
            throw new IllegalArgumentException("El rol ID y el nombre del rol son obligatorios.");
        }

        if (usuario.getPassword() == null || usuario.getPassword().length() < 8 || !validarPassword(usuario.getPassword())) {
            throw new Exception("La barra debe estar en verde. Incluye mayúsculas, minúsculas, números y símbolos en la contraseña.");
        }

        Set<UsuarioRol> usuarioRoles = new HashSet<>();
        Rol rol = new Rol();
        rol.setRolId(rolId);
        rol.setRolNombre(rolNombre);

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setUsuario(usuario);
        usuarioRol.setRol(rol);
        usuarioRoles.add(usuarioRol);

        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setPassword(bCryptPasswordEncoder.encode(usuario.getPassword()));


        return guardarUsuario(usuario, usuarioRoles);
    }

    private boolean validarPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_+=\\{\\}\\[\\]:;\"'<>,.?/\\\\|`~])[A-Za-z\\d!@#$%^&*()\\-_+=\\{\\}\\[\\]:;\"'<>,.?/\\\\|`~]{8,}$");
    }

    @Override
    public void updateUsuario(UsuarioDTO usuarioDTO, Usuario usuario) {
        if (usuarioDTO.getUsername() != null) {

            if (usuarioDTO.getUsername().length() < 8 ||
                    usuarioDTO.getUsername().length() > 20 ||
                    !usuarioDTO.getUsername().matches("[a-zA-Z0-9]+")) {
                throw new IllegalArgumentException("El username debe tener mínimo 8 caracteres y solo letras y números.");
            }

            validateUsername(usuarioDTO.getUsername(), usuario.getId());
            usuario.setUsername(usuarioDTO.getUsername());
        }

        if (usuarioDTO.getPassword() != null) {

            if (usuarioDTO.getPassword().length() < 8 || !validarPassword(usuarioDTO.getPassword())) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres y contener mayúsculas, minúsculas, números y símbolos.");
            }

            String newPasswordHash = usuarioDTO.getPassword();

            if (!newPasswordHash.equals(usuario.getPassword())) {
                usuario.setPassword(bCryptPasswordEncoder.encode(newPasswordHash));
            }
        }

        if (usuarioDTO.getEmail() != null) {
            validateEmail(usuarioDTO.getEmail(), usuario.getId());
            usuario.setEmail(usuarioDTO.getEmail());
        }

        if (usuarioDTO.getDNI() != null) {
            validateDNI(usuarioDTO.getDNI(), usuario.getId());
            usuario.setDNI(usuarioDTO.getDNI());
        }
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellidoPaterno(usuarioDTO.getApellidoPaterno());
        usuario.setApellidoMaterno(usuarioDTO.getApellidoMaterno());
        usuario.setFechaNacimiento(usuarioDTO.getFechaNacimiento());
        usuario.setTelefono(usuarioDTO.getTelefono());
    }

    private void validateUsername(String newUsername, Long usuarioId) {
        if (usuarioRepository.existsByUsername(newUsername)) {
            Usuario existingUser = usuarioRepository.findByUsername(newUsername);
            if (existingUser != null && !existingUser.getId().equals(usuarioId)) {
                throw new IllegalArgumentException("El username ya está en uso.");
            }
        }
    }

    private void validateDNI(String newDNI, Long usuarioId) {
        if (usuarioRepository.existsByDNI(newDNI)) {
            Usuario existingUser = usuarioRepository.findByDNI(newDNI);
            if (existingUser != null && !existingUser.getId().equals(usuarioId)) {
                throw new IllegalArgumentException("El DNI ya está en uso.");
            }
        }
    }

    private void validateEmail(String newEmail, Long usuarioId) {
        if (!isValidEmailDomain(newEmail)) {
            throw new IllegalArgumentException("El email debe ser de dominio @gmail.com o @hotmail.com.");
        }

        if (usuarioRepository.existsByEmail(newEmail)) {
            Usuario existingUser = usuarioRepository.findByEmail(newEmail);
            if (existingUser != null && !existingUser.getId().equals(usuarioId)) {
                throw new IllegalArgumentException("El email ya está en uso.");
            }
        }
    }

    private boolean isValidEmailDomain(String email) {
        return email.endsWith("@gmail.com") || email.endsWith("@hotmail.com");
    }


}
