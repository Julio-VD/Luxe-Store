package com.luxe.store.repository;

import com.luxe.store.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsuarioIgnoreCase(String usuario);
    Optional<Usuario> findByCorreoUsuarioIgnoreCase(String correoUsuario);
    Optional<Usuario> findFirstByRolIgnoreCase(String rol);
    List<Usuario> findAllByRolIgnoreCase(String rol);
}
