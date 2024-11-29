package com.example.aos.sistema_aos_spring_boot.Modelo.Request;

public class VerificarAsistenciaRequest {
    private Long eventoId;
    private Long usuarioId;

    public VerificarAsistenciaRequest() {
    }

    // Getters y Setters
    public Long getEventoId() {
        return eventoId;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
}