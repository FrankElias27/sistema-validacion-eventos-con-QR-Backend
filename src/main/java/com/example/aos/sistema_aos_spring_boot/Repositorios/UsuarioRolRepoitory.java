package com.example.aos.sistema_aos_spring_boot.Repositorios;

import com.example.aos.sistema_aos_spring_boot.Modelo.Rol;
import com.example.aos.sistema_aos_spring_boot.Modelo.UsuarioRol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRolRepoitory extends JpaRepository<UsuarioRol,Long> {
    List<UsuarioRol> findByRol(Rol rol);


}
