package com.example.aos.sistema_aos_spring_boot.Servicios.impl;

import com.example.aos.sistema_aos_spring_boot.Modelo.DTO.BirthdayDTO;
import com.example.aos.sistema_aos_spring_boot.Repositorios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import com.example.aos.sistema_aos_spring_boot.Modelo.Birthday;
import com.example.aos.sistema_aos_spring_boot.Modelo.Usuario;
import com.example.aos.sistema_aos_spring_boot.Repositorios.BirthdayRepository;
import com.example.aos.sistema_aos_spring_boot.Servicios.BirthdayService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Service
public class BirthdayServiceImpl implements BirthdayService {

    @Autowired
    private BirthdayRepository birthdayRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public void agregarCumpleaños(Usuario usuario, List<Birthday> nuevosCumpleaños) {

        if (nuevosCumpleaños.size() > 40) {
            throw new RuntimeException("No puedes agregar más de 40 amigos por Lista.");
        }

        int añoActual = LocalDate.now().getYear();

        List<Birthday> cumpleañosExistentes = birthdayRepository.findByUsuarioAndAño(usuario, añoActual);
        if (!cumpleañosExistentes.isEmpty()) {
            throw new RuntimeException("Ya tienes una lista de amigos para el año " + añoActual + ". Si deseas modificarla escríbenos al +51 970 932 182");
        }

        Set<String> dnisExistentes = new HashSet<>();

        for (Birthday cumple : nuevosCumpleaños) {

            if (cumple.getNombre() == null || cumple.getNombre().isEmpty() || cumple.getNombre().length() < 3 ||
                    !cumple.getNombre().matches("[a-zA-Z\\s]+")) {
                throw new RuntimeException("El nombre debe tener al menos 3 caracteres, no puede estar vacío y solo debe contener letras y espacios.");
            }

            if (cumple.getDni() == null || cumple.getDni().isEmpty() || !cumple.getDni().matches("\\d{8}")) {
                throw new RuntimeException("El DNI debe ser un número de 8 dígitos.");
            }

            if (!dnisExistentes.add(cumple.getDni())) {
                throw new RuntimeException("El DNI " + cumple.getDni() + " se repite en la lista de cumpleaños.");
            }

            cumple.setUsuario(usuario);
            cumple.setAño(añoActual);
            birthdayRepository.save(cumple);
        }
    }

    @Override
    public List<Birthday> findByUsuarioAndAño(Usuario usuario, int año) {
        return birthdayRepository.findByUsuarioAndAño(usuario, año);
    }

    @Override
    @Transactional
    public void actualizarCumpleaños(Long usuarioId, List<BirthdayDTO> cumpleañosActualizados) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        int añoActual = LocalDate.now().getYear();

        List<Birthday> cumpleañosExistentesActuales = birthdayRepository.findByUsuarioAndAño(usuario, añoActual);

        int cantidadExistente = cumpleañosExistentesActuales.size();
        int cantidadNuevos = 0;

        for (BirthdayDTO birthdayDTO : cumpleañosActualizados) {
            if (birthdayDTO.getBirthdayId() == null) {

                cantidadNuevos++;
            }
        }

        if (cantidadExistente + cantidadNuevos > 40) {
            throw new RuntimeException("No se puede agregar más de 40 invitados para el año " + añoActual);
        }

        Set<Long> idsActualizados = cumpleañosActualizados.stream()
                .map(BirthdayDTO::getBirthdayId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (BirthdayDTO birthdayDTO : cumpleañosActualizados) {
            if (birthdayDTO.getBirthdayId() != null) {

                Birthday existingBirthday = birthdayRepository.findById(birthdayDTO.getBirthdayId())
                        .orElseThrow(() -> new RuntimeException("Cumpleaños no encontrado con ID: " + birthdayDTO.getBirthdayId()));

                existingBirthday.setNombre(birthdayDTO.getNombre());
                existingBirthday.setDni(birthdayDTO.getDni());
                existingBirthday.setCreatedDate(LocalDateTime.now());
                existingBirthday.setAño(añoActual);
                existingBirthday.setUsuario(usuario);

                birthdayRepository.save(existingBirthday);
            } else {
                Birthday nuevoCumpleaños = new Birthday();
                nuevoCumpleaños.setNombre(birthdayDTO.getNombre());
                nuevoCumpleaños.setDni(birthdayDTO.getDni());
                nuevoCumpleaños.setCreatedDate(LocalDateTime.now());
                nuevoCumpleaños.setAño(añoActual);
                nuevoCumpleaños.setUsuario(usuario);
                birthdayRepository.save(nuevoCumpleaños);
            }
        }

        for (Birthday existingBirthday : cumpleañosExistentesActuales) {
            if (!idsActualizados.contains(existingBirthday.getBirthdayId())) {
                birthdayRepository.delete(existingBirthday);
            }
        }
    }





}
