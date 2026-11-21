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

    public Optional<Usuario> autenticar(String usuarioOCorreo, String contrasena) {
        if (usuarioOCorreo == null || contrasena == null) return Optional.empty();
        String dato = usuarioOCorreo.trim();
        Optional<Usuario> encontrado = usuarioRepository.findByUsuarioIgnoreCase(dato);
        if (encontrado.isEmpty()) {
            encontrado = usuarioRepository.findByCorreoUsuarioIgnoreCase(dato);
        }
        return encontrado
                .filter(u -> "Activo".equalsIgnoreCase(u.getEstado()))
                .filter(u -> u.getContrasena() != null && encoder.matches(contrasena, u.getContrasena()));
    }

    public boolean restablecerContrasenaAdmin(String nuevaContrasena) {
        Optional<Usuario> encontrado = usuarioRepository.findFirstByRolIgnoreCase("ADMIN");
        if (encontrado.isEmpty()) return false;
        Usuario admin = encontrado.get();
        if (!"ADMIN".equalsIgnoreCase(admin.getRol())) return false;
        validarNuevaContrasena(nuevaContrasena);
        admin.setContrasena(encoder.encode(nuevaContrasena));
        usuarioRepository.save(admin);
        return true;
    }

    public boolean cambiarContrasenaAdmin(String contrasenaActual, String nuevaContrasena) {
        Optional<Usuario> encontrado = usuarioRepository.findFirstByRolIgnoreCase("ADMIN");
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

    public Optional<Usuario> obtenerVendedor() {
        return usuarioRepository.findFirstByRolIgnoreCase("VENDEDOR");
    }

    public Usuario crearVendedor(String nombre, String usuario, String correo, String claveTemporal) {
        if (nombre == null || !nombre.trim().matches("[A-Za-zÁÉÍÓÚáéíóúÑñÜü ]{5,100}"))
            throw new IllegalArgumentException("El nombre completo debe tener al menos 5 caracteres y solo letras y espacios.");
        if (usuario == null || !usuario.equals(usuario.trim()) || !usuario.matches("[^\\s]{3,30}"))
            throw new IllegalArgumentException("El usuario debe tener entre 3 y 30 caracteres, sin espacios.");
        if (correo == null || !correo.equals(correo.trim()) || !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Ingrese un correo válido sin espacios y con @.");
        validarNuevaContrasena(claveTemporal);
        if (usuarioRepository.findByUsuarioIgnoreCase(usuario.trim()).isPresent()) throw new IllegalArgumentException("Ese nombre de usuario ya existe.");
        if (usuarioRepository.findByCorreoUsuarioIgnoreCase(correo.trim()).isPresent()) throw new IllegalArgumentException("Ese correo ya está registrado.");
        return usuarioRepository.save(new Usuario(null, nombre.trim(), correo.trim().toLowerCase(), usuario.trim(), encoder.encode(claveTemporal), "VENDEDOR", "Activo"));
    }

    public boolean cambiarContrasenaUsuario(Long id, String actual, String nueva) {
        Usuario u = usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("No se encontró la cuenta."));
        if (u.getContrasena() == null || !encoder.matches(actual, u.getContrasena())) return false;
        validarNuevaContrasena(nueva);
        u.setContrasena(encoder.encode(nueva));
        usuarioRepository.save(u);
        return true;
    }

    public boolean cambiarEstadoVendedor(Long id) {
        Usuario u = usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("No se encontró el vendedor."));
        if (!"VENDEDOR".equalsIgnoreCase(u.getRol())) throw new IllegalArgumentException("Solo se puede gestionar cuentas vendedor.");
        u.setEstado("Activo".equalsIgnoreCase(u.getEstado()) ? "Inactivo" : "Activo");
        usuarioRepository.save(u);
        return true;
    }

    public boolean actualizarCorreoVendedor(String correo) {
        if (correo == null || !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Ingrese un correo electrónico válido.");
        }
        Usuario vendedor = obtenerVendedor()
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la cuenta vendedor."));
        Optional<Usuario> propietario = usuarioRepository.findByCorreoUsuarioIgnoreCase(correo.trim());
        if (propietario.isPresent() && !propietario.get().getId().equals(vendedor.getId())) {
            throw new IllegalArgumentException("Ese correo ya está asociado a otra cuenta.");
        }
        vendedor.setCorreoUsuario(correo.trim().toLowerCase());
        usuarioRepository.save(vendedor);
        return true;
    }

    private void validarNuevaContrasena(String nuevaContrasena) {
        if (nuevaContrasena == null || nuevaContrasena.length() < 10) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 10 caracteres.");
        }
    }
}
