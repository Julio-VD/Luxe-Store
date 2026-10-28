package com.luxe.store.repository;
import com.luxe.store.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ProductoRepository extends JpaRepository<Producto,Long> {
 List<Producto> findByEstadoIgnoreCase(String estado); Optional<Producto> findByCodigoIgnoreCase(String codigo);
 List<Producto> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCaseOrCategoriaContainingIgnoreCase(String n,String c,String cat);
 default List<Producto> findByEstado(String estado){return findByEstadoIgnoreCase(estado);} default Optional<Producto> findByCodigo(String codigo){return findByCodigoIgnoreCase(codigo);}
}