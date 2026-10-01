package com.luxe.store.repository;

import com.luxe.store.model.Usuario;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UsuarioRepository {

    private final Map<Long, Usuario> data = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(0);

    public List<Usuario> findAll() {
        return new ArrayList<>(data.values());
    }

    public Optional<Usuario> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(data.get(id));
    }

    public Optional<Usuario> findByUsuarioAndContrasena(String usuario, String contrasena) {
        return data.values().stream()
                .filter(u -> usuario != null && usuario.equalsIgnoreCase(u.getUsuario())
                          && contrasena != null && contrasena.equals(u.getContrasena()))
                .findFirst();
    }

    public Usuario save(Usuario usuario) {
        if (usuario.getId() == null || usuario.getId() <= 0) {
            usuario.setId(idSeq.incrementAndGet());
        } else {
            idSeq.updateAndGet(curr -> Math.max(curr, usuario.getId()));
        }
        data.put(usuario.getId(), usuario);
        return usuario;
    }

    public long count() {
        return data.size();
    }
}
