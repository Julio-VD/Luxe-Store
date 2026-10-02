package com.luxe.store.service;

import com.luxe.store.model.DetalleVenta;
import com.luxe.store.model.Venta;
import com.luxe.store.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoService productoService;

    public List<Venta> obtenerTodas() {
        return ventaRepository.findAll();
    }

    public Optional<Venta> buscarPorId(Long id) {
        return ventaRepository.findById(id);
    }

    public Optional<Venta> buscarPorTicket(String ticket) {
        return ventaRepository.findByNumeroTicket(ticket);
    }

    public Venta registrarVenta(Venta venta) {
        venta.setEstado("Completado");
        Venta guardada = ventaRepository.save(venta);
        if (guardada.getNumeroTicket() == null || guardada.getNumeroTicket().isEmpty()) {
            guardada.setNumeroTicket("#TLX-" + String.format("%05d", guardada.getId() + 245));
            ventaRepository.save(guardada);
        }

        // Deducción de stock en memoria
        if (venta.getDetalles() != null) {
            for (DetalleVenta det : venta.getDetalles()) {
                if (det.getProducto() != null && det.getProducto().getId() != null) {
                    productoService.reducirStock(det.getProducto().getId(), det.getCantidad());
                }
            }
        }

        return guardada;
    }

    public Double obtenerTotalVentasMonto() {
        return ventaRepository.findAll().stream()
                .filter(v -> "Completado".equalsIgnoreCase(v.getEstado()))
                .mapToDouble(Venta::getMontoTotal)
                .sum();
    }

    public int obtenerCantidadVentas() {
        return (int) ventaRepository.findAll().stream()
                .filter(v -> "Completado".equalsIgnoreCase(v.getEstado()))
                .count();
    }

    public Map<String, Double> obtenerVentasPorCategoria() {
        Map<String, Double> mapa = new HashMap<>();
        mapa.put("Polos", 1450.00);
        mapa.put("Camisas", 2150.00);
        mapa.put("Pantalones", 1890.00);
        mapa.put("Casacas", 980.00);
        return mapa;
    }

    public Map<String, Double> obtenerVentasPorMes() {
        Map<String, Double> mapa = new LinkedHashMap<>();
        mapa.put("Mayo", 3200.00);
        mapa.put("Junio", 4500.00);
        mapa.put("Julio", 5100.00);
        mapa.put("Agosto", 6800.00);
        mapa.put("Septiembre", 7450.00);
        return mapa;
    }

    public Map<String, Integer> obtenerVentasPorMetodoPago() {
        Map<String, Integer> mapa = new HashMap<>();
        mapa.put("Efectivo", 45);
        mapa.put("Tarjeta", 38);
        mapa.put("Yape / Plin", 52);
        return mapa;
    }
}
