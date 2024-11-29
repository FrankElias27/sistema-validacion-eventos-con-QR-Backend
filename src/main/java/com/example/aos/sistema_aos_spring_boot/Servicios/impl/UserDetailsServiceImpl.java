package com.example.aos.sistema_aos_spring_boot.Servicios.impl;

import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.AccountBannedException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.UserNotVerifiedException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Usuario;
import com.example.aos.sistema_aos_spring_boot.Repositorios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = this.usuarioRepository.findByUsername(username);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }

        if (!usuario.isEnabled()) {
            throw new AccountBannedException("Tu cuenta ha sido baneada. Si necesitas asistencia, por favor comunícate al +51 970 932 182.");
        }

        if (!usuario.isVerified()) {
            throw new UserNotVerifiedException("Usuario no verificado. Por favor, revisa el correo de verificación que hemos enviado a tu email.");
        }

        return usuario;
    }

}

