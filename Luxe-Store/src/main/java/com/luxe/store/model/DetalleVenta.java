package com.luxe.store.model;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@Table(name="detalles_venta")
public class DetalleVenta implements Serializable {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.EAGER, optional=false) @JoinColumn(name="producto_id", nullable=false) private Producto producto;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="venta_id", nullable=false) private Venta venta;
    @Column(nullable=false) private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;

    public DetalleVenta() {}

    public DetalleVenta(Long id, Producto producto, Integer cantidad, Double precioUnitario, Double subtotal) {
        this.id = id;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
}
