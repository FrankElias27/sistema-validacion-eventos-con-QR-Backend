package com.example.aos.sistema_aos_spring_boot.Modelo;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "QRCode")
public class QRCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long QRCodeId;

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;

    private boolean used;

    @Column(nullable = false, unique = true, length = 2000)
    private String valorQR;

    @Version
    private Long version;


    @OneToMany(mappedBy = "qrCode",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<RegistroEventos> registroEventos3  = new HashSet<>();


    public Long getQRCodeId() {
        return QRCodeId;
    }

    public void setQRCodeId(Long QRCodeId) {
        this.QRCodeId = QRCodeId;
    }

    public String getValorQR() {
        return valorQR;
    }

    public void setValorQR(String valorQR) {
        if (valorQR == null || valorQR.length() > 2000) {
            throw new IllegalArgumentException("Valor QR inválido");
        }
        this.valorQR = valorQR;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public Set<RegistroEventos> getRegistroEventos3() {
        return registroEventos3;
    }

    public void setRegistroEventos3(Set<RegistroEventos> registroEventos3) {
        this.registroEventos3 = registroEventos3;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public QRCode() {
        this.fechaGeneracion = LocalDateTime.now();

    }
}
