package com.luxe.store.model;

import java.util.ArrayList;
import java.util.List;

public class Venta {
    private Long id;
    private String numeroTicket;
    private String fecha;
    private Cliente cliente;
    private String vendedor;
    private List<DetalleVenta> detalles = new ArrayList<>();
    private String metodoPago; // "Efectivo", "Tarjeta", "Yape/Plin"
    private Double montoTotal;
    private Double montoRecibido;
    private Double vuelto;
    private String estado; // "Completado", "Cancelado"

    public Venta() {}

    public Venta(Long id, String numeroTicket, String fecha, Cliente cliente, String vendedor, List<DetalleVenta> detalles, String metodoPago, Double montoTotal, Double montoRecibido, Double vuelto, String estado) {
        this.id = id;
        this.numeroTicket = numeroTicket;
        this.fecha = fecha;
        this.cliente = cliente;
        this.vendedor = vendedor;
        this.detalles = detalles != null ? detalles : new ArrayList<>();
        this.montoTotal = montoTotal;
        this.metodoPago = metodoPago;
        this.montoRecibido = montoRecibido;
        this.vuelto = vuelto;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroTicket() { return numeroTicket; }
    public void setNumeroTicket(String numeroTicket) { this.numeroTicket = numeroTicket; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public String getVendedor() { return vendedor; }
    public void setVendedor(String vendedor) { this.vendedor = vendedor; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public Double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(Double montoTotal) { this.montoTotal = montoTotal; }

    public Double getMontoRecibido() { return montoRecibido; }
    public void setMontoRecibido(Double montoRecibido) { this.montoRecibido = montoRecibido; }

    public Double getVuelto() { return vuelto; }
    public void setVuelto(Double vuelto) { this.vuelto = vuelto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
