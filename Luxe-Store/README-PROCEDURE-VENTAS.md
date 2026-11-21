# Procedure de ventas en Neon

1. Ejecutar `NEON-PROCEDURE.sql` en el SQL Editor de Neon **antes** de desplegar.
2. Desplegar este ZIP en Render.
3. Registrar una venta real de prueba y comprobar que el stock se descuenta una sola vez.
4. No ejecutar `CALL descontar_stock_venta(...)` sobre inventario real solo para probar: descuenta unidades.

## Flujo
`VendedorController` → `VentaService.registrarVenta` → `CALL descontar_stock_venta(id, cantidad)` → tabla `productos` → `VentaRepository` guarda `ventas` y `detalles_venta`.

El procedimiento bloquea la fila (`FOR UPDATE`), comprueba estado y stock y descuenta la cantidad. Spring usa `@Transactional` para revertir los descuentos si falla otro producto o el registro de la venta. El código Java ya **no** llama a `productoService.reducirStock`, evitando el doble descuento.

**Nota:** No se ejecutó una prueba real contra Neon ni una compilación Maven en este entorno. Crear el procedimiento antes de desplegar es obligatorio.
