package com.example.aos.sistema_aos_spring_boot.Servicios;

import com.example.aos.sistema_aos_spring_boot.Modelo.Usuario;

public interface EmailLogService {
    void deleteEmailLogsByUsuarioId(Long usuarioId);

    void eliminarRegistrosDeHoy(Usuario usuario);

    boolean existenRegistrosDeHoy(Usuario usuario);
}
