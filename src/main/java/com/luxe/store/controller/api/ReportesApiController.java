package com.luxe.store.controller.api;

import com.luxe.store.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReportesApiController {

    @Autowired
    private VentaService ventaService;

    @GetMapping("/categorias")
    public Map<String, Double> porCategoria() {
        return ventaService.obtenerVentasPorCategoria();
    }

    @GetMapping("/mensual")
    public Map<String, Double> porMes() {
        return ventaService.obtenerVentasPorMes();
    }

    @GetMapping("/metodos-pago")
    public Map<String, Integer> porMetodoPago() {
        return ventaService.obtenerVentasPorMetodoPago();
    }

    @GetMapping("/resumen")
    public Map<String, Object> resumen() {
        Map<String, Object> map = new HashMap<>();
        map.put("totalVentasMonto", ventaService.obtenerTotalVentasMonto());
        map.put("cantidadVentas", ventaService.obtenerCantidadVentas());
        return map;
    }
}
