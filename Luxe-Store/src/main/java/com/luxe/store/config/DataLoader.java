package com.luxe.store.config;

import com.luxe.store.model.Usuario;
import com.luxe.store.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner initDatabase(UsuarioRepository usuarioRepo, Environment environment) {
        return args -> {
            // Conserva las cuentas existentes. Solo crea cuentas iniciales si no hay
            // ningún usuario con ese rol en la base de datos.
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String adminPassword = environment.getProperty("ADMIN_INITIAL_PASSWORD");
            String vendedorPassword = environment.getProperty("VENDEDOR_INITIAL_PASSWORD");

            if (usuarioRepo.findFirstByRolIgnoreCase("ADMIN").isEmpty()) {
                validarClaveInicial(adminPassword, "ADMIN_INITIAL_PASSWORD");
                usuarioRepo.save(new Usuario(null, "Administrador Principal", "admin@luxe.pe",
                        "admin", encoder.encode(adminPassword), "ADMIN", "Activo"));
            }

            if (usuarioRepo.findFirstByRolIgnoreCase("VENDEDOR").isEmpty()) {
                validarClaveInicial(vendedorPassword, "VENDEDOR_INITIAL_PASSWORD");
                usuarioRepo.save(new Usuario(null, "Vendedor", "vendedor@luxe.pe",
                        "vendedor", encoder.encode(vendedorPassword), "VENDEDOR", "Activo"));
            }
        };
    }

    private void validarClaveInicial(String clave, String variable) {
        if (clave == null || clave.length() < 10) {
            throw new IllegalStateException("Configure " + variable + " con una contraseña de al menos 10 caracteres.");
        }
    }
}
