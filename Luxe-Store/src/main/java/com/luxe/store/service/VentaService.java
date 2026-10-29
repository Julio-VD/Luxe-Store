package com.luxe.store.service;

import com.luxe.store.dto.VentaDetalleDTO;
import com.luxe.store.model.DetalleVenta;
import com.luxe.store.model.Producto;
import com.luxe.store.model.Venta;
import com.luxe.store.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /** Obtiene en formato DTO únicamente los productos de la venta solicitada. */
    @Transactional(readOnly = true)
    public List<VentaDetalleDTO> obtenerDetalleVenta(Long ventaId) {
        return ventaRepository.obtenerDetallePorVentaId(ventaId);
    }

    public Optional<Venta> buscarPorId(Long id) {
        return ventaRepository.findById(id);
    }

    public Optional<Venta> buscarPorTicket(String ticket) {
        return ventaRepository.findByNumeroTicket(ticket);
    }

    @Transactional
    public Venta registrarVenta(Venta venta) {
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("Agregue al menos un producto a la venta.");
        }
        Map<Long, Integer> cantidades = new HashMap<>();
        for (DetalleVenta det : venta.getDetalles()) {
            if (det.getProducto() == null || det.getProducto().getId() == null
                    || det.getCantidad() == null || det.getCantidad() <= 0) {
                throw new IllegalArgumentException("Hay un producto o una cantidad inválida.");
            }
            cantidades.merge(det.getProducto().getId(), det.getCantidad(), Integer::sum);
        }
        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productoService.buscarPorId(entrada.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Uno de los productos ya no existe."));
            if (!"Activo".equalsIgnoreCase(producto.getEstado()) || producto.getStock() == null
                    || producto.getStock() < entrada.getValue()) {
                throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre() + ".");
            }
        }
        venta.setEstado("Completado");
        if (venta.getDetalles() != null) venta.getDetalles().forEach(det -> det.setVenta(venta));
        Venta guardada = ventaRepository.save(venta);
        if (guardada.getNumeroTicket() == null || guardada.getNumeroTicket().isEmpty()) {
            guardada.setNumeroTicket("#TLX-" + String.format("%05d", guardada.getId() + 245));
            ventaRepository.save(guardada);
        }

        // Descuento de stock persistido en PostgreSQL
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
                .mapToDouble(v -> v.getMontoTotal() == null ? 0.0 : v.getMontoTotal())
                .sum();
    }

    public int obtenerCantidadVentas() {
        return (int) ventaRepository.findAll().stream()
                .filter(v -> "Completado".equalsIgnoreCase(v.getEstado()))
                .count();
    }

    @Transactional(readOnly = true)
    public Map<String, Double> obtenerVentasPorCategoria() {
        Map<String, Double> mapa = new LinkedHashMap<>();
        for (Venta venta : ventaRepository.findAll()) {
            if (!"Completado".equalsIgnoreCase(venta.getEstado()) || venta.getDetalles() == null) continue;
            for (DetalleVenta detalle : venta.getDetalles()) {
                String categoria = detalle.getProducto() != null && detalle.getProducto().getCategoria() != null
                        ? detalle.getProducto().getCategoria() : "Sin categoría";
                mapa.merge(categoria, detalle.getSubtotal() == null ? 0.0 : detalle.getSubtotal(), Double::sum);
            }
        }
        return mapa;
    }

    @Transactional(readOnly = true)
    public Map<String, Double> obtenerVentasPorMes() {
        Map<String, Double> mapa = new LinkedHashMap<>();
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        for (Venta venta : ventaRepository.findAll()) {
            if (!"Completado".equalsIgnoreCase(venta.getEstado()) || venta.getFecha() == null) continue;
            try {
                String[] partes = venta.getFecha().split("[ /:]");
                if (partes.length >= 3) {
                    int mes = Integer.parseInt(partes[1]);
                    if (mes >= 1 && mes <= 12) {
                        mapa.merge(meses[mes - 1], venta.getMontoTotal() == null ? 0.0 : venta.getMontoTotal(), Double::sum);
                    }
                }
            } catch (NumberFormatException ignored) {
                // Fechas antiguas con otro formato no se incluyen en el gráfico mensual.
            }
        }
        return mapa;
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> obtenerVentasPorMetodoPago() {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (Venta venta : ventaRepository.findAll()) {
            if (!"Completado".equalsIgnoreCase(venta.getEstado())) continue;
            String metodo = venta.getMetodoPago() == null ? "Sin especificar" : venta.getMetodoPago();
            mapa.merge(metodo, 1, Integer::sum);
        }
        return mapa;
    }

}
