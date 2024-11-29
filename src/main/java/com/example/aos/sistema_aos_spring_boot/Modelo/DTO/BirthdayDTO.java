package com.example.aos.sistema_aos_spring_boot.Modelo.DTO;

import java.time.LocalDateTime;

public class BirthdayDTO {
    private Long birthdayId; // ID del cumpleaños, necesario para actualizar
    private String nombre;    // Nombre del amigo
    private String dni;       // DNI del amigo
    private LocalDateTime createdDate;
    private int año;      // Año del cumpleaños

    public BirthdayDTO() {
    }

    public Long getBirthdayId() {
        return birthdayId;
    }

    public void setBirthdayId(Long birthdayId) {
        this.birthdayId = birthdayId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public int getAño() {
        return año;
    }

    public void setAño(int año) {
        this.año = año;
    }
}
