package com.example.aos.sistema_aos_spring_boot.Repositorios;

import com.example.aos.sistema_aos_spring_boot.Modelo.Birthday;
import com.example.aos.sistema_aos_spring_boot.Modelo.Evento;
import com.example.aos.sistema_aos_spring_boot.Modelo.Usuario;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BirthdayRepository extends JpaRepository<Birthday,Long> {

    List<Birthday> findByUsuarioAndAño(Usuario usuario, int año);

    long countByUsuarioId(Long usuarioId);

}
