package com.example.aos.sistema_aos_spring_boot.Controladores;

import com.example.aos.sistema_aos_spring_boot.Configuraciones.JwtUtils;
import com.example.aos.sistema_aos_spring_boot.Modelo.*;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.AccountBannedException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.UserNotVerifiedException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Request.ErrorResponse;
import com.example.aos.sistema_aos_spring_boot.Servicios.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/generate-token")
    public ResponseEntity<?> generarToken(@RequestBody JwtRequest jwtRequest) throws Exception {
        try{
            autenticar(jwtRequest.getUsername(),jwtRequest.getPassword());
        }  catch (AccountBannedException e) {
            return ResponseEntity.status(403).body(new ErrorResponse(e.getMessage(), 403));
        } catch (UserNotVerifiedException e) {
            return ResponseEntity.status(403).body(new ErrorResponse(e.getMessage(), 403));
        }catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(new ErrorResponse("Credenciales inválidas. Por favor, verifica que tu username y contraseña sean correctos. ", 401));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage(), 404));
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseEntity.status(500).body(new ErrorResponse("Error inesperado: " + exception.getMessage(), 500));
        }

        UserDetails userDetails =  this.userDetailsService.loadUserByUsername(jwtRequest.getUsername());
        String token = this.jwtUtils.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    private void autenticar(String username, String password) throws DisabledException, BadCredentialsException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (InternalAuthenticationServiceException e) {
            Throwable cause = e.getCause();

            if (cause instanceof AccountBannedException) {
                throw (AccountBannedException) cause;
            } else if (cause instanceof UserNotVerifiedException) {
                throw (UserNotVerifiedException) cause;
            }
            throw e;
        }
    }

    @GetMapping("/actual-usuario")
    public Usuario obtenerUsuarioActual(Principal principal){
        return (Usuario) this.userDetailsService.loadUserByUsername(principal.getName());
    }
}