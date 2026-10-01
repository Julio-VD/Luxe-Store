package com.luxe.store.repository;

import com.luxe.store.model.Producto;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ProductoRepository {

    private final Map<Long, Producto> data = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(0);

    public List<Producto> findAll() {
        return new ArrayList<>(data.values());
    }

    public List<Producto> findByEstado(String estado) {
        return data.values().stream()
                .filter(p -> estado != null && estado.equalsIgnoreCase(p.getEstado()))
                .collect(Collectors.toList());
    }

    public Optional<Producto> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(data.get(id));
    }

    public Optional<Producto> findByCodigo(String codigo) {
        if (codigo == null) return Optional.empty();
        return data.values().stream()
                .filter(p -> codigo.equalsIgnoreCase(p.getCodigo()))
                .findFirst();
    }

    public List<Producto> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCaseOrCategoriaContainingIgnoreCase(String n, String c, String cat) {
        return data.values().stream()
                .filter(p -> (n != null && p.getNombre() != null && p.getNombre().toLowerCase().contains(n.toLowerCase()))
                          || (c != null && p.getCodigo() != null && p.getCodigo().toLowerCase().contains(c.toLowerCase()))
                          || (cat != null && p.getCategoria() != null && p.getCategoria().toLowerCase().contains(cat.toLowerCase())))
                .collect(Collectors.toList());
    }

    public Producto save(Producto producto) {
        if (producto.getId() == null || producto.getId() <= 0) {
            producto.setId(idSeq.incrementAndGet());
        } else {
            idSeq.updateAndGet(curr -> Math.max(curr, producto.getId()));
        }
        data.put(producto.getId(), producto);
        return producto;
    }

    public boolean existsById(Long id) {
        return id != null && data.containsKey(id);
    }

    public void deleteById(Long id) {
        if (id != null) {
            data.remove(id);
        }
    }

    public long count() {
        return data.size();
    }
}
