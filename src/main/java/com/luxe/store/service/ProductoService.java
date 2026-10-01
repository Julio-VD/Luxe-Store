package com.luxe.store.service;

import com.luxe.store.model.Producto;
import com.luxe.store.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> obtenerActivos() {
        return productoRepository.findByEstado("Activo");
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Optional<Producto> buscarPorCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo);
    }

    public List<Producto> buscarPorTermino(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return obtenerTodos();
        }
        return productoRepository.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCaseOrCategoriaContainingIgnoreCase(termino, termino, termino);
    }

    public Producto guardar(Producto producto) {
        if (producto.getEstado() == null) {
            producto.setEstado("Activo");
        }
        return productoRepository.save(producto);
    }

    public boolean actualizar(Producto productoActualizado) {
        if (productoActualizado.getId() != null && productoRepository.existsById(productoActualizado.getId())) {
            productoRepository.save(productoActualizado);
            return true;
        }
        return false;
    }

    public boolean inhabilitar(Long id) {
        Optional<Producto> opt = productoRepository.findById(id);
        if (opt.isPresent()) {
            Producto p = opt.get();
            p.setEstado("Inactivo");
            productoRepository.save(p);
            return true;
        }
        return false;
    }

    public boolean activar(Long id) {
        Optional<Producto> opt = productoRepository.findById(id);
        if (opt.isPresent()) {
            Producto p = opt.get();
            if (p.getStock() != null && p.getStock() > 0) {
                p.setEstado("Activo");
                productoRepository.save(p);
                return true;
            }
        }
        return false;
    }

    public boolean eliminar(Long id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean reducirStock(Long id, int cantidad) {
        Optional<Producto> opt = productoRepository.findById(id);
        if (opt.isPresent()) {
            Producto p = opt.get();
            if (p.getStock() >= cantidad) {
                p.setStock(p.getStock() - cantidad);
                if (p.getStock() == 0) {
                    p.setEstado("Agotado");
                }
                productoRepository.save(p);
                return true;
            }
        }
        return false;
    }

    public boolean aumentarStock(Long id, int cantidad) {
        if (cantidad <= 0) {
            return false;
        }

        Optional<Producto> opt = productoRepository.findById(id);
        if (opt.isPresent()) {
            Producto p = opt.get();
            int stockActual = p.getStock() == null ? 0 : p.getStock();
            p.setStock(stockActual + cantidad);

            if ("Agotado".equalsIgnoreCase(p.getEstado()) && p.getStock() > 0) {
                p.setEstado("Activo");
            }

            productoRepository.save(p);
            return true;
        }
        return false;
    }
}
