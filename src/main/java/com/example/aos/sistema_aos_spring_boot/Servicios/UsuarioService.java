package com.example.aos.sistema_aos_spring_boot.Servicios;

import com.example.aos.sistema_aos_spring_boot.Modelo.DTO.UsuarioDTO;
import com.example.aos.sistema_aos_spring_boot.Modelo.Evento;
import com.example.aos.sistema_aos_spring_boot.Modelo.Rol;
import com.example.aos.sistema_aos_spring_boot.Modelo.Usuario;
import com.example.aos.sistema_aos_spring_boot.Modelo.UsuarioRol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface UsuarioService {

    public Usuario guardarUsuario(Usuario usuario, Set<UsuarioRol> usuarioRoles) throws Exception;

    public Usuario obtenerUsuario(String username);

    public void eliminarUsuario(Long usuarioId);

    List<Usuario> obtenerUsuariosPorRol(Rol rol);

    List<Usuario> obtenerUsuariosPorRolYCumpleaños(Rol rol, LocalDate fecha);

    Page<Usuario> findAll(Pageable pageable);

    Usuario obtenerUsuarioporID(Long usuarioId);

    Usuario actualizarUsuario(Usuario usuario);

    Page<Usuario> buscarSistemaPorNombre(String nombre, Pageable pageable);

    Page<Usuario> buscarUsuarioPorNombre(String nombre, Pageable pageable);

    Page<Usuario> buscarAdminPorNombre(String nombre, Pageable pageable);

    Page<Usuario> buscarClientePorNombre(String nombre, Pageable pageable);

    Page<Usuario> buscarPorteroPorNombre(String nombre, Pageable pageable);

    long contarUsuariosCumpleanosHoyPorRol(Long roleId, int dia, int mes);

    Page<Usuario> encontrarUsuariosPorRolYCumpleaños(Long roleId, int dia, int mes, Pageable pageable);

    Page<Usuario> encontrarUsuariosPorRolYFechas(Long roleId, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);

    Set<Usuario> obtenerUsuarioClientes();

     boolean existeUsuario(String email);

    public Usuario save(Usuario usuario);

    void validateCode(String code) throws Exception;

    void changePassword(String code, String newPassword) throws Exception;

    public Usuario guardarUsuarioConRolDeUser(Usuario usuario, Long rolId, String rolNombre) throws Exception;

    public void updateUsuario(UsuarioDTO usuarioDTO, Usuario usuario);



}

