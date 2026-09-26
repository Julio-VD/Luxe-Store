package com.luxe.store.service;

import com.luxe.store.model.Producto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final List<Producto> productos = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(6);

    public ProductoService() {
        // Datos de prueba iniciales de la tienda "Luxe"
        productos.add(new Producto(1L, "PL001", "Polo Algodón Premium", "Polos", "M", "Negro", 45.00, 25, "Activo"));
        productos.add(new Producto(2L, "CM002", "Camisa Oxford Slim", "Camisas", "L", "Azul", 75.00, 12, "Activo"));
        productos.add(new Producto(3L, "PA003", "Pantalón Denim Clásico", "Pantalones", "32", "Azul Oscuro", 95.00, 4, "Activo"));
        productos.add(new Producto(4L, "PL004", "Polo Oversize Luxe", "Polos", "L", "Blanco", 55.00, 18, "Activo"));
        productos.add(new Producto(5L, "CM005", "Camisa Casual Manga Larga", "Camisas", "M", "Verde", 69.90, 8, "Activo"));
    }

    public List<Producto> obtenerTodos() {
        return new ArrayList<>(productos);
    }

    public List<Producto> obtenerActivos() {
        return productos.stream()
                .filter(p -> "Activo".equalsIgnoreCase(p.getEstado()))
                .collect(Collectors.toList());
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public Optional<Producto> buscarPorCodigo(String codigo) {
        return productos.stream()
                .filter(p -> p.getCodigo().equalsIgnoreCase(codigo))
                .findFirst();
    }

    public List<Producto> buscarPorTermino(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return obtenerTodos();
        }
        String q = termino.toLowerCase();
        return productos.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(q) ||
                             p.getCodigo().toLowerCase().contains(q) ||
                             p.getCategoria().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public Producto guardar(Producto producto) {
        if (producto.getId() == null) {
            producto.setId(idGenerator.getAndIncrement());
            if (producto.getEstado() == null) {
                producto.setEstado("Activo");
            }
            productos.add(producto);
        } else {
            actualizar(producto);
        }
        return producto;
    }

    public boolean actualizar(Producto productoActualizado) {
        Optional<Producto> opt = buscarPorId(productoActualizado.getId());
        if (opt.isPresent()) {
            Producto p = opt.get();
            p.setCodigo(productoActualizado.getCodigo());
            p.setNombre(productoActualizado.getNombre());
            p.setCategoria(productoActualizado.getCategoria());
            p.setTalla(productoActualizado.getTalla());
            p.setColor(productoActualizado.getColor());
            p.setPrecio(productoActualizado.getPrecio());
            p.setStock(productoActualizado.getStock());
            if (productoActualizado.getEstado() != null) {
                p.setEstado(productoActualizado.getEstado());
            }
            return true;
        }
        return false;
    }

    public boolean inhabilitar(Long id) {
        Optional<Producto> opt = buscarPorId(id);
        if (opt.isPresent()) {
            opt.get().setEstado("Inactivo");
            return true;
        }
        return false;
    }

    public boolean eliminar(Long id) {
        return productos.removeIf(p -> p.getId().equals(id));
    }

    public boolean reducirStock(Long id, int cantidad) {
        Optional<Producto> opt = buscarPorId(id);
        if (opt.isPresent()) {
            Producto p = opt.get();
            if (p.getStock() >= cantidad) {
                p.setStock(p.getStock() - cantidad);
                if (p.getStock() == 0) {
                    p.setEstado("Agotado");
                }
                return true;
            }
        }
        return false;
    }
}
