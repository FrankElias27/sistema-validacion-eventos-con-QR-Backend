package com.example.aos.sistema_aos_spring_boot;

import com.example.aos.sistema_aos_spring_boot.Modelo.Rol;
import com.example.aos.sistema_aos_spring_boot.Modelo.Usuario;
import com.example.aos.sistema_aos_spring_boot.Modelo.UsuarioRol;
import com.example.aos.sistema_aos_spring_boot.Servicios.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@SpringBootApplication
@EnableScheduling
public class SistemaAosSpringBootApplication implements CommandLineRunner {
	@Autowired
	private UsuarioService usuarioService;

    @Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(SistemaAosSpringBootApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		String username = "JuanAdmin04";
		try {
			Usuario usuarioExistente = usuarioService.obtenerUsuario(username);

			if (usuarioExistente == null) {
				// Solo crear el usuario si no existe
				Usuario usuario = new Usuario();
				usuario.setNombre("Juan Jerry");
				usuario.setApellidoPaterno("Espino");
				usuario.setApellidoMaterno("Figueroa");
				usuario.setUsername(username);
				usuario.setPassword(bCryptPasswordEncoder.encode("%Juan%Espino04"));
				usuario.setEmail("JuanEspino@gmail.com");
				usuario.setTelefono("988212020");
				usuario.setDNI("88888888");
				usuario.setFechaRegistro(LocalDateTime.now());
				usuario.setFechaNacimiento(LocalDate.of(1992, 3, 4));
				usuario.setVerified(true);

				Rol rol = new Rol();
				rol.setRolId(1L);
				rol.setRolNombre("ROLE_ADMIN");

				Set<UsuarioRol> usuariosRoles = new HashSet<>();
				UsuarioRol usuarioRol = new UsuarioRol();
				usuarioRol.setRol(rol);
				usuarioRol.setUsuario(usuario);
				usuariosRoles.add(usuarioRol);

				Usuario usuarioGuardado = usuarioService.guardarUsuario(usuario, usuariosRoles);
				System.out.println("Usuario creado: " + usuarioGuardado.getUsername());
			} else {
				System.out.println("El usuario ya existe: " + usuarioExistente.getUsername());
			}
		} catch (Exception e) {

			System.err.println("Error al crear el usuario: " + e.getMessage());
			e.printStackTrace();
		}
	}


}
