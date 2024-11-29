package com.example.aos.sistema_aos_spring_boot.Servicios;

import com.example.aos.sistema_aos_spring_boot.Modelo.Birthday;
import com.example.aos.sistema_aos_spring_boot.Modelo.DTO.BirthdayDTO;
import com.example.aos.sistema_aos_spring_boot.Modelo.Usuario;

import java.util.List;

public interface BirthdayService {

    void agregarCumpleaños(Usuario usuario, List<Birthday> nuevosCumpleaños);

    List<Birthday> findByUsuarioAndAño(Usuario usuario, int año);

    void actualizarCumpleaños(Long usuarioId, List<BirthdayDTO> cumpleañosActualizados);


}
