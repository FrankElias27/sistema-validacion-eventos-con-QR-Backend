package com.example.aos.sistema_aos_spring_boot.Modelo.DTO;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

public class UsuarioDTO {

    private Long id;

    @NotNull(message = "El nombre de usuario es requerido.")
    @Size(min = 3, message = "El nombre de usuario debe tener al menos 3 caracteres.")
    private String username;

    private String password; // Puedes optar por no incluir esto para evitar la exposición del hash

    @NotNull(message = "El nombre es requerido.")
    private String nombre;

    private String apellidoPaterno;
    private String apellidoMaterno;

    private LocalDate fechaNacimiento;

    @NotNull(message = "El DNI es requerido.")
    private String DNI;

    @NotNull(message = "El email es requerido.")
    @Email(message = "Formato de email inválido.")
    private String email;

    private String telefono;

    private boolean enabled = true;

    public UsuarioDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull(message = "El nombre de usuario es requerido.") @Size(min = 3, message = "El nombre de usuario debe tener al menos 3 caracteres.") String getUsername() {
        return username;
    }

    public void setUsername(@NotNull(message = "El nombre de usuario es requerido.") @Size(min = 3, message = "El nombre de usuario debe tener al menos 3 caracteres.") String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public @NotNull(message = "El nombre es requerido.") String getNombre() {
        return nombre;
    }

    public void setNombre(@NotNull(message = "El nombre es requerido.") String nombre) {
        this.nombre = nombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public @NotNull(message = "El DNI es requerido.") String getDNI() {
        return DNI;
    }

    public void setDNI(@NotNull(message = "El DNI es requerido.") String DNI) {
        this.DNI = DNI;
    }

    public @NotNull(message = "El email es requerido.") @Email(message = "Formato de email inválido.") String getEmail() {
        return email;
    }

    public void setEmail(@NotNull(message = "El email es requerido.") @Email(message = "Formato de email inválido.") String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
