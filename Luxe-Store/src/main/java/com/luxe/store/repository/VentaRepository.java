package com.luxe.store.repository;

import com.luxe.store.dto.VentaDetalleDTO;
import com.luxe.store.model.Venta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    Optional<Venta> findByNumeroTicket(String ticket);

    List<Venta> findAllByOrderByIdDesc();

    List<Venta> findByCliente_IdOrderByIdDesc(Long clienteId);

    /**
     * Obtiene únicamente los datos necesarios para el modal de una venta concreta.
     * Devuelve una fila por cada producto asociado a la venta seleccionada.
     */
    @Query("""
        SELECT new com.luxe.store.dto.VentaDetalleDTO(
            v.id,
            v.numeroTicket,
            v.fecha,
            c.nombres,
            c.tipoDocumento,
            c.numeroDocumento,
            v.vendedor,
            v.metodoPago,
            v.montoTotal,
            v.estado,
            p.codigo,
            p.nombre,
            p.categoria,
            p.talla,
            p.color,
            d.cantidad,
            d.precioUnitario,
            d.subtotal
        )
        FROM Venta v
        LEFT JOIN v.cliente c
        JOIN v.detalles d
        JOIN d.producto p
        WHERE v.id = :ventaId
        ORDER BY d.id ASC
        """)
    List<VentaDetalleDTO> obtenerDetallePorVentaId(@Param("ventaId") Long ventaId);
}
