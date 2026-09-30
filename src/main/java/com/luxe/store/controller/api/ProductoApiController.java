package com.luxe.store.controller.api;

import com.luxe.store.model.Producto;
import com.luxe.store.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoApiController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public List<Producto> listarTodos() {
        return productoService.obtenerTodos();
    }

    @GetMapping("/activos")
    public List<Producto> listarActivos() {
        return productoService.obtenerActivos();
    }

    @GetMapping("/{id}")
    public Producto obtenerPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id).orElse(null);
    }

    @PostMapping
    public Producto guardar(@RequestBody Producto producto) {
        return productoService.guardar(producto);
    }

    @PutMapping("/{id}")
    public boolean actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        producto.setId(id);
        return productoService.actualizar(producto);
    }

    @PatchMapping("/{id}/inhabilitar")
    public boolean inhabilitar(@PathVariable Long id) {
        return productoService.inhabilitar(id);
    }

    @PatchMapping("/{id}/activar")
    public boolean activar(@PathVariable Long id) {
        return productoService.activar(id);
    }

    @PatchMapping("/{id}/stock/aumentar")
    public boolean aumentarStock(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        Integer cantidad = body.get("cantidad");
        return cantidad != null && productoService.aumentarStock(id, cantidad);
    }

    @DeleteMapping("/{id}")
    public boolean eliminar(@PathVariable Long id) {
        return productoService.eliminar(id);
    }
}
