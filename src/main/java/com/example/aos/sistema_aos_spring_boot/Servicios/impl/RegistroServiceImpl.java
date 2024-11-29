package com.example.aos.sistema_aos_spring_boot.Servicios.impl;

import com.example.aos.sistema_aos_spring_boot.Enums.Asistencia;
import com.example.aos.sistema_aos_spring_boot.Enums.Registro;
import com.example.aos.sistema_aos_spring_boot.Modelo.Evento;
import com.example.aos.sistema_aos_spring_boot.Modelo.RegistroEventos;
import com.example.aos.sistema_aos_spring_boot.Repositorios.RegistroRepository;
import com.example.aos.sistema_aos_spring_boot.Servicios.RegistroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
public class RegistroServiceImpl implements RegistroService {
    @Autowired
    private RegistroRepository registroRepository;

    @Override
    @Transactional
    public RegistroEventos agregarRegistroEventos(RegistroEventos registroEventos) {
        validateRegistroEventos(registroEventos);
        return registroRepository.save(registroEventos);
    }

    @Override
    public boolean verificarAsistencia(Long eventoId, Long usuarioId) {
        return registroRepository.existsByEvento_EventoIdAndUsuario_IdAndEstadoRegistro(eventoId, usuarioId, Registro.CONFIRMADO);
    }

    @Override
    @Transactional
    public RegistroEventos actualizarRegistro(RegistroEventos registroEventos) {
        validateRegistroEventos(registroEventos);
        return registroRepository.save(registroEventos);
    }

    @Override
    public RegistroEventos obtenerRegistroEventoPorEventoYUsuario(Long eventoId, Long usuarioId) {
        RegistroEventos registro = registroRepository.findByEvento_EventoIdAndUsuario_Id(eventoId, usuarioId);
        if (registro == null) {
            throw new EntityNotFoundException("Registro no encontrado para el evento y usuario dados.");
        }
        return registro;
    }

    @Override
    public Page<RegistroEventos> getRegistroEventosByEventoNombre(Long eventoId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "registroEventoId"));
        return registroRepository.findByEvento_EventoId_AndEstadoAsistencia(eventoId, Asistencia.ASISTIO , pageable);
    }

    private void validateRegistroEventos(RegistroEventos registroEventos) {
        if (registroEventos == null) {
            throw new IllegalArgumentException("El registro de evento no puede ser nulo.");
        }
        if (registroEventos.getUsuario() == null || registroEventos.getEvento() == null) {
            throw new IllegalArgumentException("El registro debe tener un usuario y un evento asociados.");
        }
    }


}
