package com.luxe.store.service;

import com.luxe.store.model.Usuario;
import com.luxe.store.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> autenticar(String usuario, String contrasena) {
        return usuarioRepository.findByUsuarioIgnoreCase(usuario)
                .filter(u -> "Activo".equalsIgnoreCase(u.getEstado()))
                .filter(u -> u.getContrasena() != null && encoder.matches(contrasena, u.getContrasena()));
    }

    public boolean restablecerContrasenaAdmin(String nuevaContrasena) {
        Optional<Usuario> encontrado = usuarioRepository.findByUsuarioIgnoreCase("admin");
        if (encontrado.isEmpty()) return false;
        Usuario admin = encontrado.get();
        if (!"ADMIN".equalsIgnoreCase(admin.getRol())) return false;
        validarNuevaContrasena(nuevaContrasena);
        admin.setContrasena(encoder.encode(nuevaContrasena));
        usuarioRepository.save(admin);
        return true;
    }

    public boolean cambiarContrasenaAdmin(String contrasenaActual, String nuevaContrasena) {
        Optional<Usuario> encontrado = usuarioRepository.findByUsuarioIgnoreCase("admin");
        if (encontrado.isEmpty()) return false;
        Usuario admin = encontrado.get();
        if (!"ADMIN".equalsIgnoreCase(admin.getRol()) || !encoder.matches(contrasenaActual, admin.getContrasena())) {
            return false;
        }
        validarNuevaContrasena(nuevaContrasena);
        admin.setContrasena(encoder.encode(nuevaContrasena));
        usuarioRepository.save(admin);
        return true;
    }

    private void validarNuevaContrasena(String nuevaContrasena) {
        if (nuevaContrasena == null || nuevaContrasena.length() < 10) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 10 caracteres.");
        }
    }
}
