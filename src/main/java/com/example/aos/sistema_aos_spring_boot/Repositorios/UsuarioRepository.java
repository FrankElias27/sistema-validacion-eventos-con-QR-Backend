package com.example.aos.sistema_aos_spring_boot.Repositorios;

import com.example.aos.sistema_aos_spring_boot.Modelo.Rol;
import com.example.aos.sistema_aos_spring_boot.Modelo.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UsuarioRepository extends JpaRepository<Usuario,Long> {

     Usuario findByUsername(String username);

    Page<Usuario> findByNombreContaining(String nombre, Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByDNI(String dni);

    @Query("SELECT COUNT(u) FROM Usuario u JOIN u.usuarioRoles ur WHERE ur.rol.rolId = :roleId AND DAY(u.fechaNacimiento) = :dia AND MONTH(u.fechaNacimiento) = :mes")
    long contarUsuariosCumpleanosHoyPorRol(@Param("roleId") Long roleId, @Param("dia") int dia, @Param("mes") int mes);

    @Query("SELECT u FROM Usuario u JOIN u.usuarioRoles ur WHERE ur.rol.rolId = :roleId AND DAY(u.fechaNacimiento) = :dia AND MONTH(u.fechaNacimiento) = :mes")
    Page<Usuario> encontrarUsuariosPorRolYCumpleaños(@Param("roleId") Long roleId, @Param("dia") int dia, @Param("mes") int mes, Pageable pageable);

    @Query("SELECT u FROM Usuario u JOIN u.usuarioRoles ur " +
            "WHERE ur.rol.rolId = :roleId " +
            "AND ((DATE_FORMAT(u.fechaNacimiento, '%m-%d') >= DATE_FORMAT(:fechaInicio, '%m-%d') " +
            "AND DATE_FORMAT(u.fechaNacimiento, '%m-%d') <= DATE_FORMAT(:fechaFin, '%m-%d')))")
    Page<Usuario> findByRolIdAndCumpleanoBetween(@Param("roleId") Long roleId,
                                                  @Param("fechaInicio") LocalDate fechaInicio,
                                                  @Param("fechaFin") LocalDate fechaFin,
                                                  Pageable pageable);



    Usuario findByEmail(String email);

    Usuario findByDNI(String dni);

    @Query("SELECT u FROM Usuario u JOIN u.usuarioRoles ur WHERE ur.rol.rolId = :roleId")
    Set<Usuario> findUsuariosByRolId(@Param("roleId") Long roleId);
}
