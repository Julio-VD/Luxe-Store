package com.luxe.store.controller.api;

import com.luxe.store.model.Venta;
import com.luxe.store.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "*")
public class VentaApiController {

    @Autowired
    private VentaService ventaService;

    @GetMapping
    public List<Venta> listarTodas() {
        return ventaService.obtenerTodas();
    }

    @PostMapping
    public Venta registrarVenta(@RequestBody Venta venta) {
        return ventaService.registrarVenta(venta);
    }
}
