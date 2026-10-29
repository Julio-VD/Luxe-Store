package com.luxe.store.dto;

/**
 * DTO de lectura para mostrar los datos de una venta y sus productos.
 * No es una entidad JPA ni representa una tabla de la base de datos.
 */
public class VentaDetalleDTO {

    private final Long ventaId;
    private final String numeroTicket;
    private final String fecha;
    private final String nombresCliente;
    private final String tipoDocumento;
    private final String numeroDocumento;
    private final String vendedor;
    private final String metodoPago;
    private final Double montoTotal;
    private final String estado;
    private final String codigoProducto;
    private final String nombreProducto;
    private final String categoria;
    private final String talla;
    private final String color;
    private final Integer cantidad;
    private final Double precioUnitario;
    private final Double subtotal;

    public VentaDetalleDTO(Long ventaId, String numeroTicket, String fecha,
            String nombresCliente, String tipoDocumento, String numeroDocumento,
            String vendedor, String metodoPago, Double montoTotal, String estado,
            String codigoProducto, String nombreProducto, String categoria,
            String talla, String color, Integer cantidad, Double precioUnitario,
            Double subtotal) {
        this.ventaId = ventaId;
        this.numeroTicket = numeroTicket;
        this.fecha = fecha;
        this.nombresCliente = nombresCliente;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.vendedor = vendedor;
        this.metodoPago = metodoPago;
        this.montoTotal = montoTotal;
        this.estado = estado;
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
        this.categoria = categoria;
        this.talla = talla;
        this.color = color;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public Long getVentaId() { return ventaId; }
    public String getNumeroTicket() { return numeroTicket; }
    public String getFecha() { return fecha; }
    public String getNombresCliente() { return nombresCliente; }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getVendedor() { return vendedor; }
    public String getMetodoPago() { return metodoPago; }
    public Double getMontoTotal() { return montoTotal; }
    public String getEstado() { return estado; }
    public String getCodigoProducto() { return codigoProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public String getCategoria() { return categoria; }
    public String getTalla() { return talla; }
    public String getColor() { return color; }
    public Integer getCantidad() { return cantidad; }
    public Double getPrecioUnitario() { return precioUnitario; }
    public Double getSubtotal() { return subtotal; }
}
