package com.luxe.store.service;

import com.luxe.store.model.Usuario;
import com.luxe.store.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> autenticar(String usuario, String contrasena) {
        return usuarioRepository.findByUsuarioAndContrasena(usuario, contrasena);
    }
}
