package com.luxe.store.service;

import com.luxe.store.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final List<Usuario> usuarios = new ArrayList<>();

    public UsuarioService() {
        // Usuarios iniciales de prueba
        usuarios.add(new Usuario(1L, "Administrador Principal", "admin@luxe.pe", "admin", "1234", "ADMIN", "Activo"));
        usuarios.add(new Usuario(2L, "Juan Pérez", "vendedor@luxe.pe", "vendedor", "1234", "VENDEDOR", "Activo"));
    }

    public List<Usuario> obtenerTodos() {
        return new ArrayList<>(usuarios);
    }

    public Optional<Usuario> autenticar(String usuario, String contrasena) {
        return usuarios.stream()
                .filter(u -> u.getUsuario().equalsIgnoreCase(usuario.trim()) && u.getContrasena().equals(contrasena))
                .findFirst();
    }
}
