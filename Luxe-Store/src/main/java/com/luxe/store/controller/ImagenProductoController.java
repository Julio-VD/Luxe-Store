package com.luxe.store.controller;

import com.luxe.store.model.Producto;
import com.luxe.store.service.ProductoService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.concurrent.TimeUnit;

@Controller
public class ImagenProductoController {
    private final ProductoService productos;
    public ImagenProductoController(ProductoService productos) { this.productos = productos; }

    @GetMapping("/productos/{id}/imagen.jpg")
    @ResponseBody
    public ResponseEntity<byte[]> imagen(@PathVariable Long id) {
        Producto producto = productos.buscarPorId(id).orElse(null);
        if (producto == null || producto.getImagen() == null || producto.getImagen().length == 0)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG)
            .cacheControl(CacheControl.noCache()).body(producto.getImagen());
    }
}
