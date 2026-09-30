package com.luxe.store.repository;

import com.luxe.store.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByEstado(String estado);
    Optional<Producto> findByCodigo(String codigo);
    List<Producto> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCaseOrCategoriaContainingIgnoreCase(String n, String c, String cat);
}
