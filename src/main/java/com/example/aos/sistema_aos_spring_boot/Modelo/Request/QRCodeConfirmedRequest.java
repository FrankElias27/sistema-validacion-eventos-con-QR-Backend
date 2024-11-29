package com.example.aos.sistema_aos_spring_boot.Modelo.Request;

public class QRCodeConfirmedRequest {
    private String event;
    private String user;
    private String dni;

    public QRCodeConfirmedRequest() {
    }

    // Getters y Setters
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
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