package com.example.aos.sistema_aos_spring_boot.Modelo;

import com.example.aos.sistema_aos_spring_boot.Enums.Asistencia;
import com.example.aos.sistema_aos_spring_boot.Enums.Registro;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RegistroEvento", indexes = {
        @Index(columnList = "evento_id"),
        @Index(columnList = "usuario_id"),
        @Index(columnList = "qr_code_id")
})
public class RegistroEventos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long registroEventoId;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Enumerated(EnumType.STRING)
    private Registro estadoRegistro;


    private String portero;

    @Enumerated(EnumType.STRING)
    private Asistencia estadoAsistencia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "qr_code_id", nullable = false)
    private QRCode qrCode;

    public Long getRegistroEventoId() {
        return registroEventoId;
    }

    public void setRegistroEventoId(Long registroEventoId) {
        this.registroEventoId = registroEventoId;
    }


    public Registro getEstadoRegistro() {
        return estadoRegistro;
    }

    public void setEstadoRegistro(Registro estadoRegistro) {
        this.estadoRegistro = estadoRegistro;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public QRCode getQrCode() {
        return qrCode;
    }

    public void setQrCode(QRCode qrCode) {
        this.qrCode = qrCode;
    }

    public String getPortero() {
        return portero;
    }

    public void setPortero(String portero) {
        this.portero = portero;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Asistencia getEstadoAsistencia() {
        return estadoAsistencia;
    }

    public void setEstadoAsistencia(Asistencia estadoAsistencia) {
        this.estadoAsistencia = estadoAsistencia;
    }

    public RegistroEventos() {
    }
}
