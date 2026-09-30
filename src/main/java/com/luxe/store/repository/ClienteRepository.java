package com.luxe.store.repository;

import com.luxe.store.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByEstado(String estado);
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);
}
