package com.luxe.store.service;

import com.luxe.store.model.Cliente;
import com.luxe.store.model.DetalleVenta;
import com.luxe.store.model.Producto;
import com.luxe.store.model.Venta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class VentaService {

    private final List<Venta> ventas = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(4);

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ClienteService clienteService;

    public VentaService() {
        // Inicializar con historial de ventas de prueba para los gráficos y tablas
    }

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void initData() {
        Cliente c1 = clienteService.buscarPorId(1L).orElse(null);
        Cliente c2 = clienteService.buscarPorId(2L).orElse(null);

        Producto p1 = productoService.buscarPorId(1L).orElse(null);
        Producto p2 = productoService.buscarPorId(2L).orElse(null);
        Producto p4 = productoService.buscarPorId(4L).orElse(null);

        if (p1 != null && p2 != null) {
            List<DetalleVenta> dv1 = new ArrayList<>();
            dv1.add(new DetalleVenta(1L, p1, 2, p1.getPrecio(), p1.getPrecio() * 2));
            dv1.add(new DetalleVenta(2L, p2, 1, p2.getPrecio(), p2.getPrecio() * 1));
            ventas.add(new Venta(1L, "#TLX-00245", "03/09/2026", c1, "Juan Pérez", dv1, "Yape/Plin", 165.00, 165.00, 0.0, "Completado"));
        }

        if (p4 != null) {
            List<DetalleVenta> dv2 = new ArrayList<>();
            dv2.add(new DetalleVenta(3L, p4, 3, p4.getPrecio(), p4.getPrecio() * 3));
            ventas.add(new Venta(2L, "#TLX-00244", "03/09/2026", c2, "María López", dv2, "Tarjeta", 165.00, 165.00, 0.0, "Completado"));
        }

        if (p1 != null && p4 != null) {
            List<DetalleVenta> dv3 = new ArrayList<>();
            dv3.add(new DetalleVenta(4L, p1, 4, p1.getPrecio(), p1.getPrecio() * 4));
            dv3.add(new DetalleVenta(5L, p4, 2, p4.getPrecio(), p4.getPrecio() * 2));
            ventas.add(new Venta(3L, "#TLX-00243", "03/09/2026", c1, "Juan Pérez", dv3, "Efectivo", 290.00, 300.00, 10.0, "Completado"));
        }
    }

    public List<Venta> obtenerTodas() {
        return new ArrayList<>(ventas);
    }

    public Optional<Venta> buscarPorId(Long id) {
        return ventas.stream()
                .filter(v -> v.getId().equals(id))
                .findFirst();
    }

    public Optional<Venta> buscarPorTicket(String ticket) {
        return ventas.stream()
                .filter(v -> v.getNumeroTicket().equalsIgnoreCase(ticket.trim()))
                .findFirst();
    }

    public Venta registrarVenta(Venta venta) {
        venta.setId(idGenerator.getAndIncrement());
        venta.setEstado("Completado");
        if (venta.getNumeroTicket() == null || venta.getNumeroTicket().isEmpty()) {
            venta.setNumeroTicket("#TLX-" + String.format("%05d", venta.getId() + 245));
        }

        // Actualizar stock de los productos
        for (DetalleVenta det : venta.getDetalles()) {
            if (det.getProducto() != null && det.getProducto().getId() != null) {
                productoService.reducirStock(det.getProducto().getId(), det.getCantidad());
            }
        }

        ventas.add(0, venta); // Añadir al inicio para que las recientes salgan primero
        return venta;
    }

    public Double obtenerTotalVentasMonto() {
        return ventas.stream()
                .filter(v -> "Completado".equalsIgnoreCase(v.getEstado()))
                .mapToDouble(Venta::getMontoTotal)
                .sum();
    }

    public int obtenerCantidadVentas() {
        return (int) ventas.stream()
                .filter(v -> "Completado".equalsIgnoreCase(v.getEstado()))
                .count();
    }

    // Datos estadísticos para los gráficos de Avance 2 (Chart.js)
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
