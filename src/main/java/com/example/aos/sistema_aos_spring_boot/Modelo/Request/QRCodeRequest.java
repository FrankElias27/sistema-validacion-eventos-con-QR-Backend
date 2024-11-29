package com.example.aos.sistema_aos_spring_boot.Modelo.Request;

public class QRCodeRequest {
    private Long event;
    private String user;
    private String dni;

    public QRCodeRequest() {
    }

    // Getters y Setters
    public Long getEvent() {
        return event;
    }

    public void setEvent(Long event) {
        this.event = event;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }
}
