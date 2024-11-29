package com.example.aos.sistema_aos_spring_boot.Modelo;

import com.example.aos.sistema_aos_spring_boot.Enums.Visibilidad;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Evento")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventoId;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String Nombre;

    @Lob
    private byte[] Imagen;

    private LocalDateTime fechaEvento;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING) // Importante: especifica que se usará el nombre del enum
    private Visibilidad visibilidad= Visibilidad.NORMAL;

    private Integer cantidadQR;

    private String codeIdentify;

    private boolean activo = true;

    @OneToMany(mappedBy = "evento",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<RegistroEventos> registroEventos  = new HashSet<>();

    public Long getEventoId() {
        return eventoId;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public byte[] getImagen() {
        return Imagen;
    }

    public void setImagen(byte[] imagen) {
        Imagen = imagen;
    }



    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Set<RegistroEventos> getRegistroEventos() {
        return registroEventos;
    }

    public void setRegistroEventos(Set<RegistroEventos> registroEventos) {
        this.registroEventos = registroEventos;
    }

    public Visibilidad getVisibilidad() {
        return visibilidad;
    }

    public void setVisibilidad(Visibilidad visibilidad) {
        this.visibilidad = visibilidad;
    }

    public LocalDateTime getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(LocalDateTime fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getCantidadQR() {
        return cantidadQR;
    }

    public void setCantidadQR(Integer cantidadQR) {
        this.cantidadQR = cantidadQR;
    }

    public String getCodeIdentify() {
        return codeIdentify;
    }

    public void setCodeIdentify(String codeIdentify) {
        this.codeIdentify = codeIdentify;
    }

    public Evento() {
    }
}
